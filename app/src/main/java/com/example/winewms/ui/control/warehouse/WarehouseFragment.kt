package com.example.winewms.ui.control.warehouse

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.SalesDataWrapper
import com.example.winewms.data.model.SearchWineViewModel
import com.example.winewms.data.model.WineInvoice
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WinesWrapper
import com.example.winewms.databinding.FragmentWarehouseBinding
import com.example.winewms.ui.account.invoices.InvoicesAdapter
import com.example.winewms.ui.account.invoices.onInvoicesClickListener
import com.example.winewms.ui.control.warehouse.adapter.warehouse.DeliveryOrderAdapter
import com.example.winewms.ui.control.warehouse.adapter.warehouse.WarehouseSearchAdapter
import com.example.winewms.ui.control.warehouse.adapter.warehouse.onDeliveryOrderClickListener
import com.example.winewms.ui.control.warehouse.adapter.warehouse.onWarehouseSearchClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar


class WarehouseFragment : Fragment(), onWarehouseSearchClickListener, onDeliveryOrderClickListener {

    private var _binding: FragmentWarehouseBinding? = null
    private val binding get() = _binding!!

    private val wineApi: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }
    private val searchWineViewModel: SearchWineViewModel by activityViewModels()

    lateinit var invoiceList: List<WineInvoice>

    // Filters
    private val selectedWineTypes = mutableListOf<String>()
    private var harvestYearStart: String? = null
    private var harvestYearEnd: String? = null
    private var minPrice = 0
    private var maxPrice = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentWarehouseBinding.inflate(inflater, container, false)

        //Setup vertical recycler view to display searched wines
        setupRecyclerView()

        // Clean data in the ViewModel
        searchWineViewModel.clearSearchWineViewModel()

        //Observe list of wines from Search Wine View Model
        observeWineList()

        //Check wines with low stock level
        getWinesWithLowStock()

        //Get customer orders to dispatch products
        getCustomerOrders()

        // Additional setup for search and filter UI
        binding.imgWarehouseSearch.setOnClickListener {
            val query = binding.txtWarehouseWineSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                hideKeyboard()
                fetchDataWithFilters(query)
            } else {
                Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        // Filter button click listener
        binding.imgWarehouseSearchFilter.setOnClickListener {
            showFilterPopup(it)
        }

        return binding.root
    }

    private fun getWinesWithLowStock() {
        val filters = mutableMapOf<String, String>()
        filters["min_stock"] = "200"  //minimum stock level required

        val apiCall = wineApi.getLowStockWines(filters = filters)
        apiCall.enqueue(object : Callback<WinesWrapper> {
            override fun onFailure(call: Call<WinesWrapper>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }
            override fun onResponse(call: Call<WinesWrapper>, response: Response<WinesWrapper>) {
                if (response.isSuccessful) {
                    val listOfWines = response.body()?.wines
                    binding.recyclerViewCriticalStockLevel.apply {
                        layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
                        adapter = listOfWines?.let { WarehouseSearchAdapter(it, this@WarehouseFragment, context) }
                    }
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    private fun observeWineList() {
        searchWineViewModel.wineList.observe(viewLifecycleOwner, Observer { listOfWines ->
            val adapter = WarehouseSearchAdapter(listOfWines, this, context)
            binding.recyclerViewWarehouseWines.adapter = adapter
            // Display recycler view if not empty or show "No results" message based on the list size
            if (listOfWines != null) {
                if (listOfWines.isNotEmpty()) {
                    binding.recyclerViewWarehouseWines.visibility = View.VISIBLE
                    binding.labelWinesFound.visibility = View.VISIBLE
                    binding.divider17.visibility = View.VISIBLE
                } else {
                    binding.recyclerViewWarehouseWines.visibility = View.GONE
                    binding.labelWinesFound.visibility = View.GONE
                    binding.divider17.visibility = View.GONE
                }
            }
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerViewWarehouseWines.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = WarehouseSearchAdapter(emptyList(), this@WarehouseFragment, context)
        }
    }

    override fun onWarehouseSearchClickListener(wineModel: WineModel) {

    }

    private fun fetchDataWithFilters(query: String, minPrice: Int? = null, maxPrice: Int? = null) {
        val filters = mutableMapOf<String, String>()
        filters["name"] = query  // Wine name query

        if (selectedWineTypes.isNotEmpty()) {
            filters["type"] = selectedWineTypes.joinToString(",")
            Log.d("SearchFragment", "Wine type filters applied: ${filters["type"]}")
        }

        harvestYearStart?.let { filters["min_harvest"] = it }
        harvestYearEnd?.let { filters["max_harvest"] = it }

        // Add minPrice and maxPrice to filters if provided
        minPrice?.let { filters["min_price"] = it.toString() }
        maxPrice?.let { filters["max_price"] = it.toString() }

        val apiCall = wineApi.getWines(filters = filters)
        apiCall.enqueue(object : Callback<DataWrapper> {
            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.wines?.let { searchWineViewModel.setWineList(it) }
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    private fun showFilterPopup(anchorView: View) {
        val inflater = LayoutInflater.from(context)
        val popupView = inflater.inflate(R.layout.filter_popup, null)
        val popupWindow = PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

        // Retrieve checkboxes for wine types
        val redCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_red)
        val whiteCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_white)
        val roseCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_rose)
        val sparklingCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_sparkling)
        val dessertCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_dessert_wine)
        val orangeCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_orange_wine)

        // Initialize the CheckBox states based on previously selected wine types
        redCheckBox.isChecked = selectedWineTypes.contains("red")
        whiteCheckBox.isChecked = selectedWineTypes.contains("white")
        roseCheckBox.isChecked = selectedWineTypes.contains("rose")
        sparklingCheckBox.isChecked = selectedWineTypes.contains("sparkling")
        dessertCheckBox.isChecked = selectedWineTypes.contains("dessert")
        orangeCheckBox.isChecked = selectedWineTypes.contains("orange")

        // Set up harvest year NumberPickers with previously selected values
        val startPicker = popupView.findViewById<NumberPicker>(R.id.numberPickerHarvestYearStart)
        val endPicker = popupView.findViewById<NumberPicker>(R.id.numberPickerHarvestYearEnd)
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val years = arrayOf("Any") + (currentYear downTo 2000).map { it.toString() }.toTypedArray()

        startPicker.minValue = 0
        startPicker.maxValue = years.size - 1
        startPicker.displayedValues = years
        endPicker.minValue = 0
        endPicker.maxValue = years.size - 1
        endPicker.displayedValues = years

        // Set picker values based on saved harvest year range, or "Any" if not set
        startPicker.value = years.indexOf(harvestYearStart ?: "Any")
        endPicker.value = years.indexOf(harvestYearEnd ?: "Any")

        // Price range TextViews
        val minPriceTextView = popupView.findViewById<TextView>(R.id.et_min_price)
        val maxPriceTextView = popupView.findViewById<TextView>(R.id.et_max_price)
        minPriceTextView.text = minPrice.toString()
        maxPriceTextView.text = maxPrice.toString()

        // Price range adjustment buttons
        popupView.findViewById<Button>(R.id.btn_decrement_min_price).setOnClickListener {
            if (minPrice > 0) {
                minPrice -= 1
                minPriceTextView.text = minPrice.toString()
            }
        }
        popupView.findViewById<Button>(R.id.btn_increment_min_price).setOnClickListener {
            minPrice += 1
            minPriceTextView.text = minPrice.toString()
        }
        popupView.findViewById<Button>(R.id.btn_decrement_max_price).setOnClickListener {
            if (maxPrice > minPrice) {
                maxPrice -= 1
                maxPriceTextView.text = maxPrice.toString()
            }
        }
        popupView.findViewById<Button>(R.id.btn_increment_max_price).setOnClickListener {
            maxPrice += 1
            maxPriceTextView.text = maxPrice.toString()
        }

        // Apply button to save the current filter configuration
        popupView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            // Save the selected wine types
            selectedWineTypes.clear()
            if (redCheckBox.isChecked) selectedWineTypes.add("red")
            if (whiteCheckBox.isChecked) selectedWineTypes.add("white")
            if (roseCheckBox.isChecked) selectedWineTypes.add("rose")
            if (sparklingCheckBox.isChecked) selectedWineTypes.add("sparkling")
            if (dessertCheckBox.isChecked) selectedWineTypes.add("dessert")
            if (orangeCheckBox.isChecked) selectedWineTypes.add("orange")

            // Save the harvest year range
            harvestYearStart = years.getOrNull(startPicker.value).takeIf { it != "Any" }
            harvestYearEnd = years.getOrNull(endPicker.value).takeIf { it != "Any" }

            // Save the price range
            minPrice = minPriceTextView.text.toString().toIntOrNull() ?: 0
            maxPrice = maxPriceTextView.text.toString().toIntOrNull() ?: 1000

            // Clear search box if needed and dismiss popup
            binding.txtWarehouseWineSearch.text.clear()
            popupWindow.dismiss()

            // Apply filters with updated values
            fetchDataWithFilters(
                query = binding.txtWarehouseWineSearch.text.toString().trim(),
                minPrice = minPrice,
                maxPrice = maxPrice
            )
        }

        // Clear button click listener to reset controls
        popupView.findViewById<Button>(R.id.btn_clear_filters).setOnClickListener {
            // Reset checkboxes
            redCheckBox.isChecked = false
            whiteCheckBox.isChecked = false
            roseCheckBox.isChecked = false
            sparklingCheckBox.isChecked = false
            dessertCheckBox.isChecked = false
            orangeCheckBox.isChecked = false

            // Reset NumberPickers to "Any"
            startPicker.value = 0
            endPicker.value = 0

            // Reset price range
            minPriceTextView.text = "0"
            maxPriceTextView.text = "1000"

            // Clear the stored filter variables
            selectedWineTypes.clear()
            harvestYearStart = null
            harvestYearEnd = null
            minPrice = 0
            maxPrice = 1000
        }

        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtWarehouseWineSearch.windowToken, 0)
    }

    // Function to get customer's order from backend
    private fun getCustomerOrders() {

        //Fetch data from api
        val apiCall = wineApi.getOrdersNotDispatched("0") //0 not dispatched.

        //Asynchronous call to request sales orders by customer's id
        apiCall.enqueue(object : Callback<SalesDataWrapper> {
            override fun onFailure(call: Call<SalesDataWrapper>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<SalesDataWrapper>, response: Response<SalesDataWrapper>) {
                if (response.isSuccessful) {
                    val dataWrapper = response.body()
                    if (dataWrapper != null) {
                        invoiceList = dataWrapper.invoices

                        //load list of orders on recycler view
                        loadOrders()
                    }
                }
            }
        })
    }

    // Function to load list of orders on recycler view
    private fun loadOrders() {
        //load recycler view
        val adapter = DeliveryOrderAdapter(invoiceList, this@WarehouseFragment, context)
        binding.recyclerViewDeliveryOrders.adapter = adapter
        binding.recyclerViewDeliveryOrders.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
    }

    override fun onDeliveryOrderClickListener(wineInvoice: WineInvoice) {

    }
}
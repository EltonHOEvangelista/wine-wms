package com.example.winewms.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.SearchWineViewModel
import com.example.winewms.databinding.FragmentSearchBinding
import com.example.winewms.ui.search.adapter.searched.OnSearchedWinesClickListener
import com.example.winewms.ui.search.adapter.searched.SearchedWinesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.CheckBox
import android.widget.Button
import android.widget.NumberPicker
import androidx.core.content.ContextCompat
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.winewms.R
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.data.model.CartWineViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class SearchFragment : Fragment(), OnSearchedWinesClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    //private lateinit var binding: FragmentSearchBinding
    private val searchWineViewModel: SearchWineViewModel by activityViewModels()
    private val wineApi: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }
    private val cartWineViewModel: CartWineViewModel by activityViewModels()

    // Filters
    private val selectedWineTypes = mutableListOf<String>()
    private var harvestYearStart: String? = null
    private var harvestYearEnd: String? = null
    private var hasInitialized = false  // New flag to ensure setup runs only once

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        //binding = FragmentSearchBinding.inflate(inflater)

        // Show "No results found" message initially
        //binding.txtNoResults.visibility = View.VISIBLE

        setupRecyclerView()
        observeWineList()

        // Retrieve the wineModelId from Home Fragment
        val wineModelId = arguments?.getString("wineModelId")

        // Optionally, check for null if necessary
        wineModelId?.let {
            fetchDataWithWineId(wineModelId)
        } ?: run {
            // Handle the case where wineModelId is null
            val searchText = arguments?.getString("searchText")
            val wineType = arguments?.getString("wineType")

            when {
                searchText != null -> {
                    binding.txtWineSearch.setText(searchText)
                    fetchDataWithFilters(searchText)
                }
                wineType != null -> {
                    selectedWineTypes.clear()
                    selectedWineTypes.add(wineType)
                    fetchDataWithFilters("")
                }
                else -> {
                    binding.txtNoResults.visibility = View.VISIBLE
                }
            }
        }

        // Search button click listener
        binding.imgSearch.setOnClickListener {
            val query = binding.txtWineSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                hideKeyboard()
                fetchDataWithFilters(query)
            } else {
                Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        // Filter button click listener
        binding.imgFilter.setOnClickListener {
            showFilterPopup(it)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewSearchedWines.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = SearchedWinesAdapter(emptyList(), this@SearchFragment)
        }
    }

    private fun observeWineList() {
        searchWineViewModel.wineList.observe(viewLifecycleOwner, Observer { listOfWines ->
            val adapter = SearchedWinesAdapter(listOfWines, this)
            binding.recyclerViewSearchedWines.adapter = adapter

            // Show or hide the "No results" message based on the list size
            binding.txtNoResults.visibility = if (listOfWines.isEmpty()) View.VISIBLE else View.GONE
        })
    }

    private fun fetchDataWithWineId(query: String) {

        val apiCall = wineApi.getWineById(query)
        apiCall.enqueue(object : Callback<WineModel> {
            override fun onFailure(call: Call<WineModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<WineModel>, response: Response<WineModel>) {
                if (response.isSuccessful) {
                    // Initialize a mutable list
                    val wineList = mutableListOf<WineModel>()

                    // Create the WineModel object
                    val wine = response.body()

                    // Add the wine to the list
                    if (wine != null) {
                        wineList.add(wine)
                    }

                    // Update the ViewModel with the list
                    searchWineViewModel.setWineList(wineList)
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }

        })
    }

    private fun fetchDataWithFilters(query: String) {
        val filters = mutableMapOf<String, String>()
        filters["name"] = query  // Wine name query

        if (selectedWineTypes.isNotEmpty()) {
            filters["type"] = selectedWineTypes.joinToString(",")
            Log.d("SearchFragment", "Wine type filters applied: ${filters["type"]}")
        }

        harvestYearStart?.let { filters["min_harvest"] = it }
        harvestYearEnd?.let { filters["max_harvest"] = it }

        val apiCall = wineApi.getAllWines(filters = filters)
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

        val redCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_red)
        val whiteCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_white)
        val roseCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_rose)
        val sparklingCheckBox = popupView.findViewById<CheckBox>(R.id.filter_option_sparkling)

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

        popupView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            selectedWineTypes.clear()
            if (redCheckBox.isChecked) selectedWineTypes.add("red")
            if (whiteCheckBox.isChecked) selectedWineTypes.add("white")
            if (roseCheckBox.isChecked) selectedWineTypes.add("rose")
            if (sparklingCheckBox.isChecked) selectedWineTypes.add("sparkling")

            harvestYearStart = if (startPicker.value != 0) years[startPicker.value] else null
            harvestYearEnd = if (endPicker.value != 0) years[endPicker.value] else null

            binding.txtWineSearch.text.clear()
            popupWindow.dismiss()
            fetchDataWithFilters(binding.txtWineSearch.text.toString().trim())
        }

        popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    override fun onSearchedWinesClickListener(wineModel: WineModel) {
        Toast.makeText(context, "Selected wine: ${wineModel.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onBuyClick(wineModel: WineModel) {
        val existingItem = cartWineViewModel.cartItems.value?.find { it.wine.id == wineModel.id }
        existingItem?.apply {
            if (quantity < wineModel.stock) quantity += 1 else Toast.makeText(context, "Only ${wineModel.stock} items available", Toast.LENGTH_SHORT).show()
        } ?: run {
            if (wineModel.stock > 0) {
                cartWineViewModel.updateCartItems(cartWineViewModel.cartItems.value.orEmpty().toMutableList().apply {
                    add(CartItemModel(wine = wineModel, quantity = 1))
                })
                Toast.makeText(context, "${wineModel.name} added to cart", Toast.LENGTH_SHORT).show()

                // Update cart badge
                updateCardBadge()

            } else {
                Toast.makeText(context, "${wineModel.name} is out of stock", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateCardBadge() {

        // Find your BottomNavigationView
        val navView: BottomNavigationView? = activity?.findViewById(R.id.nav_view)
        //val navView: BottomNavigationView = binding.root.findViewById(R.id.nav_view)

        val menuItemId = R.id.navigation_cart
        val badgeDrawable = navView!!.getOrCreateBadge(menuItemId)

        // Set the badge number
        badgeDrawable.isVisible = true  // Show the badge
        badgeDrawable.number = cartWineViewModel.cartItems.value?.size ?: 0  //Set the number to show on the badge

        // Optionally customize the badge's appearance
        badgeDrawable.badgeTextColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        badgeDrawable.backgroundColor = ContextCompat.getColor(requireContext(), R.color.DarkRedWine)
    }

    override fun onDetailsClick(wineModel: WineModel) {
        Toast.makeText(context, "Viewing details for ${wineModel.name}", Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtWineSearch.windowToken, 0)
    }
}

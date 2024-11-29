package com.example.winewms.ui.control.manageWines

import android.content.Context
import android.os.Bundle
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
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.databinding.FragmentControlBinding
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.WarehouseModel
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.FragmentAdminBinding
import com.example.winewms.ui.control.manageWines.AdminWineAdapter
import com.example.winewms.ui.control.manageWines.OnAdminWineClickListener
import com.google.android.material.internal.ViewUtils.hideKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AdminFragment : Fragment() , OnAdminWineClickListener {

    private lateinit var binding: FragmentAdminBinding
    private val wineApi = WineApi.retrofit.create(WineApiService::class.java)
    private lateinit var adminWineAdapter: AdminWineAdapter

    private val selectedWineTypes = mutableListOf<String>()
    private var harvestYearStart: String? = null
    private var harvestYearEnd: String? = null
    private var minPrice = 0
    private var maxPrice = 1000

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentAdminBinding.inflate(inflater, container, false)
        setupClickListeners()
        setupRecyclerView()
        fetchWineData()
        setupSearch()
        return binding.root
    }

    private fun setupClickListeners() {

        // Set click listener for Add Wine Button
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_addWineFragment)
        }
    }
    private fun setupRecyclerView() {
        adminWineAdapter = AdminWineAdapter(emptyList(), this)
        binding.recyclerViewWines.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adminWineAdapter
        }
    }

    // search function
    private fun setupSearch() {
        binding.apply {
            // Search button click
            imgSearch.setOnClickListener {
                val query = txtWineSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideKeyboard()
                    fetchDataWithFilters(query)
                }
            }

            // Filter button click
            imgFilter.setOnClickListener {
                showFilterPopup(it)
            }
        }
    }

    private fun fetchDataWithFilters(query: String, minPrice: Int? = null, maxPrice: Int? = null) {
        val filters = mutableMapOf<String, String>().apply {
            put("name", query)
            selectedWineTypes.takeIf { it.isNotEmpty() }?.let { put("type", it.joinToString(",")) }
            harvestYearStart?.let { put("min_harvest", it) }
            harvestYearEnd?.let { put("max_harvest", it) }
            minPrice?.let { put("min_price", it.toString()) }
            maxPrice?.let { put("max_price", it.toString()) }
        }

        wineApi.getWines(filters = filters).enqueue(object : Callback<DataWrapper> {
            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.wines?.let { adminWineAdapter.updateData(it) }
                } else {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun fetchWineData(query: String = "", minPrice: Int? = null, maxPrice: Int? = null) {
        val filters = mutableMapOf<String, String>().apply {
            query.takeIf { it.isNotEmpty() }?.let { put("name", it) }
            selectedWineTypes.takeIf { it.isNotEmpty() }?.let { put("type", it.joinToString(",")) }
            harvestYearStart?.let { put("min_harvest", it) }
            harvestYearEnd?.let { put("max_harvest", it) }
            minPrice?.let { put("min_price", it.toString()) }
            maxPrice?.let { put("max_price", it.toString()) }
        }

        wineApi.getWines(page = 1, limit = 10, filters = filters).enqueue(object : Callback<DataWrapper> {
            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.wines?.let { adminWineAdapter.updateData(it) }
                } else {
                    Toast.makeText(context, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
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

            // Initialize checkboxes with current filters
            redCheckBox.isChecked = selectedWineTypes.contains("red")
            whiteCheckBox.isChecked = selectedWineTypes.contains("white")
            roseCheckBox.isChecked = selectedWineTypes.contains("rose")
            sparklingCheckBox.isChecked = selectedWineTypes.contains("sparkling")
            dessertCheckBox.isChecked = selectedWineTypes.contains("dessert")
            orangeCheckBox.isChecked = selectedWineTypes.contains("orange")

            // Setup year pickers
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

            // Set current values
            startPicker.value = years.indexOf(harvestYearStart ?: "Any")
            endPicker.value = years.indexOf(harvestYearEnd ?: "Any")

            // Price range
            val minPriceTextView = popupView.findViewById<TextView>(R.id.et_min_price)
            val maxPriceTextView = popupView.findViewById<TextView>(R.id.et_max_price)
            minPriceTextView.text = minPrice.toString()
            maxPriceTextView.text = maxPrice.toString()

            // Price controls
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

            // Apply filters
            popupView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
                selectedWineTypes.clear()
                if (redCheckBox.isChecked) selectedWineTypes.add("red")
                if (whiteCheckBox.isChecked) selectedWineTypes.add("white")
                if (roseCheckBox.isChecked) selectedWineTypes.add("rose")
                if (sparklingCheckBox.isChecked) selectedWineTypes.add("sparkling")
                if (dessertCheckBox.isChecked) selectedWineTypes.add("dessert")
                if (orangeCheckBox.isChecked) selectedWineTypes.add("orange")

                harvestYearStart = years.getOrNull(startPicker.value).takeIf { it != "Any" }
                harvestYearEnd = years.getOrNull(endPicker.value).takeIf { it != "Any" }

                minPrice = minPriceTextView.text.toString().toIntOrNull() ?: 0
                maxPrice = maxPriceTextView.text.toString().toIntOrNull() ?: 1000

                binding.txtWineSearch.text.clear()
                popupWindow.dismiss()

                fetchWineData(
                    query = binding.txtWineSearch.text.toString().trim(),
                    minPrice = minPrice,
                    maxPrice = maxPrice
                )
            }

            // Clear filters
            popupView.findViewById<Button>(R.id.btn_clear_filters).setOnClickListener {
                selectedWineTypes.clear()
                harvestYearStart = null
                harvestYearEnd = null
                minPrice = 0
                maxPrice = 1000

                // Reset UI
                redCheckBox.isChecked = false
                whiteCheckBox.isChecked = false
                roseCheckBox.isChecked = false
                sparklingCheckBox.isChecked = false
                dessertCheckBox.isChecked = false
                orangeCheckBox.isChecked = false
                startPicker.value = 0
                endPicker.value = 0
                minPriceTextView.text = "0"
                maxPriceTextView.text = "1000"

                // Fetch without filters
                fetchWineData()

                popupWindow.dismiss()
            }

            popupWindow.showAsDropDown(anchorView, 0, 0)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtWineSearch.windowToken, 0)
    }




    //function get warehouse
    private fun fetchWarehouseData(onComplete: (List<WarehouseModel>) -> Unit) {
        wineApi.getWarehouses().enqueue(object : Callback<List<WarehouseModel>> {
            override fun onResponse(call: Call<List<WarehouseModel>>, response: Response<List<WarehouseModel>>) {
                if (response.isSuccessful) {
                    val warehouseList = response.body() ?: emptyList()
                    onComplete(warehouseList)
                } else {
                    Toast.makeText(context, "Failed to fetch warehouses", Toast.LENGTH_SHORT).show()
                    onComplete(emptyList())
                }
            }

            override fun onFailure(call: Call<List<WarehouseModel>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                onComplete(emptyList())
            }
        })
    }



    override fun onEditClick(wine: WineModel) {
        navigateToEditWineFragment(wine)
    }

    override fun onDeleteClick(wine: WineModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Wine")
            .setMessage("Are you sure you want to delete ${wine.name}?")
            .setPositiveButton("Delete") { _, _ ->
                deleteWine(wine.id)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWine(wineId: String) {
        wineApi.deleteWine(wineId).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Wine deleted successfully", Toast.LENGTH_SHORT).show()
                    fetchWineData()
                } else {
                    Toast.makeText(context, "Failed to delete wine", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
    private fun navigateToEditWineFragment(wine: WineModel?) {
        val bundle = Bundle().apply {
            putString("wineId", wine?.id)
        }
        findNavController().navigate(
            R.id.action_adminFragment_to_editWineFragment,
            bundle
        )
    }

}

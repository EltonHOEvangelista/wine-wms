package com.example.winewms.ui.control.manageWines

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
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.PopupWindow
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.databinding.FragmentControlBinding
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.setFragmentResultListener
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.ResponseModel
import com.example.winewms.data.model.WarehouseModel
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.FragmentAdminBinding
import com.example.winewms.ui.control.manageWines.AdminWineAdapter
import com.example.winewms.ui.control.manageWines.OnAdminWineClickListener
import com.google.android.material.internal.ViewUtils.hideKeyboard
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class AdminFragment : Fragment(), OnAdminWineClickListener {
    private var _binding: FragmentAdminBinding? = null
    private val binding get() = _binding!!

    private val wineViewModel: WineViewModel by activityViewModels()
    private val wineApi: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }

    // Filters
    private val selectedWineTypes = mutableListOf<String>()
    private var harvestYearStart: String? = null
    private var harvestYearEnd: String? = null
    private var minPrice = 0
    private var maxPrice = 1000

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminBinding.inflate(inflater, container, false)

        setupFloatingActionButton()
        setupRecyclerView()
        setupSearch()
        setupFragmentResultListener()
        observeWineList()

        return binding.root
    }

    private fun setupFloatingActionButton() {
        binding.floatingActionButton.apply {
            visibility = View.VISIBLE
            setOnClickListener {
                findNavController().navigate(R.id.action_adminFragment_to_addWineFragment)
            }
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerViewWines.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = AdminWineAdapter(emptyList(), this@AdminFragment)
        }
    }

    private fun setupSearch() {
        binding.apply {
            imgSearch.setOnClickListener {
                val query = txtWineSearch.text.toString().trim()
                if (query.isNotEmpty()) {
                    hideKeyboard()
                    fetchDataWithFilters(query)
                } else {
                    Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
                }
            }

            imgFilter.setOnClickListener {
                showFilterPopup(it)
            }
        }
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener("adminFilterRequest") { _, bundle ->
            bundle.getString("searchText")?.let { searchText ->
                binding.txtWineSearch.setText(searchText)
                fetchDataWithFilters(searchText)
            }

            bundle.getString("wineType")?.let { wineType ->
                binding.txtWineSearch.text.clear()
                selectedWineTypes.clear()
                selectedWineTypes.add(wineType)
                fetchDataWithFilters("")
            }

            bundle.getString("wineModelId")?.let { wineModelId ->
                binding.txtWineSearch.text.clear()
                fetchDataWithWineId(wineModelId)
            }
        }
    }

    private fun observeWineList() {
        wineViewModel.wineList.observe(viewLifecycleOwner) { wines ->
            (binding.recyclerViewWines.adapter as AdminWineAdapter).updateData(wines)
        }
    }

    private fun fetchDataWithWineId(query: String) {
        val apiCall = wineApi.getWineById(query)
        apiCall.enqueue(object : Callback<WineModel> {
            override fun onFailure(call: Call<WineModel>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.d("API Service Failure", t.message.toString())
            }
            override fun onResponse(call: Call<WineModel>, response: Response<WineModel>) {
                if (response.isSuccessful) {
                    response.body()?.let { wine ->
                        wineViewModel.setWineList(listOf(wine))
                    }
                } else {
                    Log.d("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    private fun fetchDataWithFilters(query: String) {
        val filters = mutableMapOf<String, String>()
        filters["name"] = query

        if (selectedWineTypes.isNotEmpty()) {
            filters["type"] = selectedWineTypes.joinToString(",")
        }

        harvestYearStart?.let { filters["min_harvest"] = it }
        harvestYearEnd?.let { filters["max_harvest"] = it }
        filters["min_price"] = minPrice.toString()
        filters["max_price"] = maxPrice.toString()

        val apiCall = wineApi.getWines(filters = filters)
        apiCall.enqueue(object : Callback<DataWrapper> {
            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.wines?.let { wineViewModel.setWineList(it) }
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    override fun onEditClick(wine: WineModel) {
        val bundle = Bundle().apply {
            putString("wineId", wine.id)
        }
        findNavController().navigate(R.id.action_adminFragment_to_editWineFragment, bundle)
    }

    override fun onDeleteClick(wine: WineModel) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Wine")
            .setMessage("Are you sure you want to delete ${wine.name}?")
            .setPositiveButton("Delete") { _, _ -> deleteWine(wine.id) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteWine(wineId: String) {
        wineApi.deleteWine(wineId).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Wine deleted successfully", Toast.LENGTH_SHORT).show()
                    fetchDataWithFilters(binding.txtWineSearch.text.toString().trim())
                } else {
                    Toast.makeText(context, "Failed to delete wine", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(context, "Error during deletion", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtWineSearch.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

        // Initialize checkbox states
        redCheckBox.isChecked = selectedWineTypes.contains("red")
        whiteCheckBox.isChecked = selectedWineTypes.contains("white")
        roseCheckBox.isChecked = selectedWineTypes.contains("rose")
        sparklingCheckBox.isChecked = selectedWineTypes.contains("sparkling")
        dessertCheckBox.isChecked = selectedWineTypes.contains("dessert")
        orangeCheckBox.isChecked = selectedWineTypes.contains("orange")

        // Set up harvest year NumberPickers
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

        startPicker.value = years.indexOf(harvestYearStart ?: "Any")
        endPicker.value = years.indexOf(harvestYearEnd ?: "Any")

        // Set up price range controls
        val minPriceTextView = popupView.findViewById<TextView>(R.id.et_min_price)
        val maxPriceTextView = popupView.findViewById<TextView>(R.id.et_max_price)
        minPriceTextView.text = minPrice.toString()
        maxPriceTextView.text = maxPrice.toString()

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

        // Set up filter buttons
        popupView.findViewById<Button>(R.id.btn_apply_filters).setOnClickListener {
            // Save selected wine types
            selectedWineTypes.clear()
            if (redCheckBox.isChecked) selectedWineTypes.add("red")
            if (whiteCheckBox.isChecked) selectedWineTypes.add("white")
            if (roseCheckBox.isChecked) selectedWineTypes.add("rose")
            if (sparklingCheckBox.isChecked) selectedWineTypes.add("sparkling")
            if (dessertCheckBox.isChecked) selectedWineTypes.add("dessert")
            if (orangeCheckBox.isChecked) selectedWineTypes.add("orange")

            // Save harvest year range
            harvestYearStart = years.getOrNull(startPicker.value).takeIf { it != "Any" }
            harvestYearEnd = years.getOrNull(endPicker.value).takeIf { it != "Any" }

            // Save price range
            minPrice = minPriceTextView.text.toString().toIntOrNull() ?: 0
            maxPrice = maxPriceTextView.text.toString().toIntOrNull() ?: 1000

            // Clear search box and apply filters
            binding.txtWineSearch.text.clear()
            popupWindow.dismiss()
            fetchDataWithFilters("")
        }

        popupView.findViewById<Button>(R.id.btn_clear_filters).setOnClickListener {
            // Reset checkboxes
            redCheckBox.isChecked = false
            whiteCheckBox.isChecked = false
            roseCheckBox.isChecked = false
            sparklingCheckBox.isChecked = false
            dessertCheckBox.isChecked = false
            orangeCheckBox.isChecked = false

            // Reset NumberPickers
            startPicker.value = 0
            endPicker.value = 0

            // Reset price range
            minPriceTextView.text = "0"
            maxPriceTextView.text = "1000"

            // Clear filter variables
            selectedWineTypes.clear()
            harvestYearStart = null
            harvestYearEnd = null
            minPrice = 0
            maxPrice = 1000

            binding.txtWineSearch.text.clear()
        }

        popupWindow.showAsDropDown(anchorView, 0, 0)
    }
}
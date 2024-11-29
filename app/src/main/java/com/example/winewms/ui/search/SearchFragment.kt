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
import android.widget.TextView
import androidx.fragment.app.setFragmentResultListener
import com.example.winewms.R
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.data.model.CartWineViewModel
import com.example.winewms.ui.account.AccountViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.util.Calendar

class SearchFragment : Fragment(), OnSearchedWinesClickListener {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchWineViewModel: SearchWineViewModel by activityViewModels()
    private val wineApi: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }
    private val cartWineViewModel: CartWineViewModel by activityViewModels()
    private val isAdmin: Boolean by lazy {
        val accountViewModel: AccountViewModel by activityViewModels()
        accountViewModel.account.value?.type == 1
    }

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
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        setupRecyclerView()
        observeWineList()

        setFragmentResultListener("searchFilterRequest") { _, bundle ->
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

        binding.imgSearch.setOnClickListener {
            val query = binding.txtWineSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                hideKeyboard()
                fetchDataWithFilters(query)
            } else {
                Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        binding.imgFilter.setOnClickListener {
            showFilterPopup(it)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewSearchedWines.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = SearchedWinesAdapter(emptyList(), this@SearchFragment, isAdmin)
        }
    }

    private fun observeWineList() {
        searchWineViewModel.wineList.observe(viewLifecycleOwner, Observer { listOfWines ->
            val adapter = SearchedWinesAdapter(listOfWines, this, isAdmin)
            binding.recyclerViewSearchedWines.adapter = adapter

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
                    response.body()?.let { wine ->
                        searchWineViewModel.setWineList(listOf(wine))
                    }
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    private fun fetchDataWithFilters(query: String, minPrice: Int? = null, maxPrice: Int? = null) {
        val filters = mutableMapOf<String, String>()
        filters["name"] = query

        if (selectedWineTypes.isNotEmpty()) {
            filters["type"] = selectedWineTypes.joinToString(",")
        }

        harvestYearStart?.let { filters["min_harvest"] = it }
        harvestYearEnd?.let { filters["max_harvest"] = it }
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
        // Existing code for showing the filter popup
    }

    override fun onSearchedWinesClickListener(wineModel: WineModel) {
        Toast.makeText(context, "Selected wine: ${wineModel.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onBuyClick(wineModel: WineModel) {
        // Existing implementation
    }

    override fun onEditClick(wineModel: WineModel) {
        // Handle edit logic for admin
        Toast.makeText(context, "Edit wine: ${wineModel.name}", Toast.LENGTH_SHORT).show()
    }

    private fun updateCardBadge() {
        // Existing implementation
    }

    override fun onDetailsClick(wineModel: WineModel) {
        // Existing implementation
    }

    private fun hideKeyboard() {
        // Existing implementation
    }
}

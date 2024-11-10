package com.example.winewms.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.FragmentSearchBinding
import com.example.winewms.ui.search.adapter.searched.OnSearchedWinesClickListener
import com.example.winewms.ui.search.adapter.searched.SearchedWinesAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Context
import android.view.inputmethod.InputMethodManager


class SearchFragment : Fragment(), OnSearchedWinesClickListener {

    private lateinit var binding: FragmentSearchBinding
    private val wineViewModel: WineViewModel by activityViewModels()

    // Initialize the Wine API
    private val wineApi: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)
        setupRecyclerView()

        // Set click listener on search icon
        binding.imgSearch.setOnClickListener {
            val query = binding.txtWineSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                Log.d("SearchFragment", "Search icon clicked with query: $query")
                Toast.makeText(context, "Searching for: $query", Toast.LENGTH_SHORT).show()

                hideKeyboard()

                fetchDataWithFilters(query)
            } else {
                Toast.makeText(context, "Please enter a search term", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewSearchedWines.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = SearchedWinesAdapter(emptyList(), this@SearchFragment) // Uses the new card layout
        }
    }

    private fun fetchDataWithFilters(query: String) {
        val filters = mutableMapOf<String, String>()
        filters["name"] = query
        val apiCall = wineApi.getAllWines(filters = filters)

        apiCall.enqueue(object : Callback<DataWrapper> {
            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(requireContext(), "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    val dataWrapper = response.body()
                    dataWrapper?.let {
                        if (it.wines.isEmpty()) {
                            // No results found
                            binding.txtNoResults.visibility = View.VISIBLE
                            binding.recyclerViewSearchedWines.visibility = View.GONE
                        } else {
                            // Wines found
                            binding.txtNoResults.visibility = View.GONE
                            binding.recyclerViewSearchedWines.visibility = View.VISIBLE
                            // Update RecyclerView adapter with results
                            binding.recyclerViewSearchedWines.adapter = SearchedWinesAdapter(it.wines, this@SearchFragment)
                            // Update ViewModel with the fetched data
                            wineViewModel.setWineList(it.wines)
                        }
                    }
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    // Implement the button actions within the fragment
    override fun onSearchedWinesClickListener(wineModel: WineModel) {
        Toast.makeText(context, "Selected wine: ${wineModel.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onBuyClick(wineModel: WineModel) {
        Toast.makeText(context, "Buying ${wineModel.name}", Toast.LENGTH_SHORT).show()
        // Logic for buying wine
    }

    override fun onDetailsClick(wineModel: WineModel) {
        Toast.makeText(context, "Viewing details for ${wineModel.name}", Toast.LENGTH_SHORT).show()
        // Logic for showing wine details
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtWineSearch.windowToken, 0)
    }
    
}

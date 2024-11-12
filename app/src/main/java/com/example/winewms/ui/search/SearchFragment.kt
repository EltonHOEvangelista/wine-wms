package com.example.winewms.ui.search

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.data.model.CartWineViewModel

class SearchFragment : Fragment(), OnSearchedWinesClickListener {

    private lateinit var binding: FragmentSearchBinding
    private val searchWineViewModel: SearchWineViewModel by activityViewModels()
    private val wineApi: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }
    private val cartWineViewModel: CartWineViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSearchBinding.inflate(inflater)

        setupRecyclerView()
        observeWineList()

        binding.imgSearch.setOnClickListener {
            val query = binding.txtWineSearch.text.toString().trim()
            if (query.isNotEmpty()) {
                Log.d("SearchFragment", "Search icon clicked with query: $query")
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
            adapter = SearchedWinesAdapter(emptyList(), this@SearchFragment)
        }
    }

    private fun observeWineList() {
        searchWineViewModel.wineList.observe(viewLifecycleOwner, Observer { listOfWines ->
            val adapter = SearchedWinesAdapter(listOfWines, this)
            binding.recyclerViewSearchedWines.adapter = adapter
            binding.txtNoResults.visibility = if (listOfWines.isEmpty()) View.VISIBLE else View.GONE
        })
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
                        searchWineViewModel.setWineList(it.wines)
                    }
                } else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    override fun onSearchedWinesClickListener(wineModel: WineModel) {
        Toast.makeText(context, "Selected wine: ${wineModel.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onBuyClick(wineModel: WineModel) {
        // Check if the item is already in the cart
        val existingItem = cartWineViewModel.cartItems.value?.find { it.wine.id == wineModel.id }
        if (existingItem != null) {
            // Item exists, check if adding one more exceeds stock
            if (existingItem.quantity < wineModel.stock) {
                existingItem.quantity += 1
                Toast.makeText(context, "Increased quantity of ${wineModel.name}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Only ${wineModel.stock} items available in stock", Toast.LENGTH_SHORT).show()
            }
        } else {
            // Item does not exist, add as new CartItemModel if stock is available
            if (wineModel.stock > 0) {
                val newCartItem = CartItemModel(wine = wineModel, quantity = 1)
                val updatedCart = cartWineViewModel.cartItems.value.orEmpty().toMutableList()
                updatedCart.add(newCartItem)
                cartWineViewModel.updateCartItems(updatedCart)
                Toast.makeText(context, "${wineModel.name} added to cart", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "${wineModel.name} is out of stock", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDetailsClick(wineModel: WineModel) {
        Toast.makeText(context, "Viewing details for ${wineModel.name}", Toast.LENGTH_SHORT).show()
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.txtWineSearch.windowToken, 0)
    }
}

package com.example.winewms.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.FragmentHomeBinding
import com.example.winewms.ui.home.adapter.featured.FeaturedWinesAdapter
import com.example.winewms.ui.home.adapter.featured.onFeaturedWinesClickListener
import androidx.core.os.bundleOf
import com.example.winewms.MainActivity
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.SearchWineViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment(), onFeaturedWinesClickListener {

    lateinit var binding: FragmentHomeBinding
    lateinit var wineList: List<WineModel>

    //variable used to transfer objects among activities and fragments
    private val wineViewModel: WineViewModel by activityViewModels()
    private val searchWineViewModel: SearchWineViewModel by activityViewModels()

    //Instantiate Wine Api
    var wineApi = WineApi.retrofit.create(WineApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

        //Setup click listeners
        setClickListeners()

        //load featured wine to Home Fragment. Function to fetch data from backend using Wine API
        getFeaturedWines()

        // Load featured wines
        loadFeaturedWinesIntoRecyclerView()

        return binding.root
    }

    private fun setClickListeners() {

        // Set up button click listeners for filtering
        binding.btnRedWine.setOnClickListener {
            navigateToSearchWithFilter("red")
        }
        binding.btnRoseWine.setOnClickListener {
            navigateToSearchWithFilter("rose")
        }
        binding.btnWhiteWine.setOnClickListener {
            navigateToSearchWithFilter("white")
        }
        binding.btnSparklingWine.setOnClickListener {
            navigateToSearchWithFilter("sparkling")
        }

        // Set up text search functionality
        binding.imgSearch.setOnClickListener {
            val searchText = binding.txtWineSearch.text.toString().trim()
            if (searchText.isNotEmpty()) {
                navigateToSearchWithText(searchText)
            }
        }
    }

    //Function to fetch featured wine from Backend and load them in WineViewModel.
    private fun getFeaturedWines() {

        // Set up filter for discounts great of equal to 15%
        val filters = mutableMapOf<String, String>()
        filters["discount"] = "0.15"
        // Fetch filtered data from api
        val apiCall = wineApi.getWines(filters = filters)

        //Asynchronous call to fetch data from Wine's Api
        apiCall.enqueue(object: Callback<DataWrapper> {
            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(context, "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }
            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    //Successfully fetched data
                    val dataWrapper = response.body()
                    if (dataWrapper != null) {
                        wineList = dataWrapper.wines
                        //Loading Wine View Model. It's required to share the wineModel object among fragments
                        wineViewModel.setWineList(wineList)
                        searchWineViewModel.setWineList(wineList)  //????
                    }
                }
                else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    private fun navigateToSearchWithFilter(wineType: String) {
        val bundle = bundleOf("wineType" to wineType)
        parentFragmentManager.setFragmentResult("searchFilterRequest", bundle)

        (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.nav_view)
            .selectedItemId = R.id.navigation_search
    }

    private fun navigateToSearchWithText(searchText: String) {
        val bundle = bundleOf("searchText" to searchText)
        parentFragmentManager.setFragmentResult("searchFilterRequest", bundle)

        (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.nav_view)
            .selectedItemId = R.id.navigation_search
    }

    //function to load featured wines into recycler view reading data from View Model
    private fun loadFeaturedWinesIntoRecyclerView() {

        wineViewModel.wineList.observe(viewLifecycleOwner, Observer { listOfWines ->
            val adapter = FeaturedWinesAdapter(listOfWines, this)
            binding.recyclerViewFeaturedWines.adapter = adapter
            binding.recyclerViewFeaturedWines.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        })
    }

    // function to navigate to search fragment using NavController, passing wine id
    override fun onFeaturedWinesClickListener(wineModel: WineModel) {
        val bundle = bundleOf("wineModelId" to wineModel.id)
        parentFragmentManager.setFragmentResult("searchFilterRequest", bundle)

        // Switch to the SearchFragment
        (requireActivity() as MainActivity).findViewById<BottomNavigationView>(R.id.nav_view)
            .selectedItemId = R.id.navigation_search
    }
}
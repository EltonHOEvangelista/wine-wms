package com.example.winewms.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment(), onFeaturedWinesClickListener {

    lateinit var binding: FragmentHomeBinding

    //variable used to transfer objects among activities and fragments
    private val wineViewModel: WineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentHomeBinding.inflate(inflater)

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

        // Load featured wines
        loadFeaturedWinesIntoRecyclerView()

        return binding.root
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
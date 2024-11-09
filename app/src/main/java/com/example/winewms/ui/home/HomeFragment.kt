package com.example.winewms.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.FragmentHomeBinding
import com.example.winewms.ui.home.adapter.featured.FeaturedWinesAdapter
import com.example.winewms.ui.home.adapter.featured.onFeaturedWinesClickListener

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

        //load featured wines into recycler view reading data from View Model
        loadFeaturedWinesIntoRecyclerView()

        return binding.root
    }

    //function to load featured wines into recycler view reading data from View Model
    private fun loadFeaturedWinesIntoRecyclerView() {

        wineViewModel.wineList.observe(viewLifecycleOwner, Observer { listOfWines ->
            val adapter = FeaturedWinesAdapter(listOfWines, this)
            binding.recyclerViewFeaturedWines.adapter = adapter
            binding.recyclerViewFeaturedWines.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        })
    }

    override fun onFeaturedWinesClickListener(wineModel: WineModel) {
        TODO("Not yet implemented")
    }
}
package com.example.winewms.ui.control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.FragmentControlBinding
import com.example.winewms.ui.control.adapter.AdminWinesAdapter
import com.example.winewms.ui.control.adapter.OnAdminWinesClickListener
import retrofit2.Response
import retrofit2.Call
import retrofit2.Callback


class ControlFragment : Fragment(), OnAdminWinesClickListener {

    private lateinit var binding: FragmentControlBinding
    private lateinit var adapter: AdminWinesAdapter
    private val wineApiService: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }
    private var wineList = mutableListOf<WineModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = FragmentControlBinding.inflate(inflater, container, false)

        setupRecyclerView()
        loadWines()

        binding.floatingActionButton.setOnClickListener {

        }

        return binding.root
    }

    private fun setupRecyclerView() {
        adapter = AdminWinesAdapter(wineList, this)
        binding.recyclerViewFeaturedWines.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = this@ControlFragment.adapter
        }
    }

    private fun loadWines() {
        wineApiService.getAllWines().enqueue(object : Callback<DataWrapper> {
            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    wineList.clear()
                    response.body()?.wines?.let { wineList.addAll(it) }
                    adapter.updateWineList(wineList)
                }
            }

            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(context, "Failed to load wines: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onEditStockClick(wine: WineModel, newStock: Int) {
        wine.stock = newStock
        wineApiService.updateWine(wine.id, wine).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Stock updated!", Toast.LENGTH_SHORT).show()
                    adapter.notifyDataSetChanged()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(context, "Failed to update stock: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDeleteClick(wine: WineModel) {
        wineApiService.deleteWine(wine.id).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    wineList.remove(wine)
                    adapter.updateWineList(wineList)
                    Toast.makeText(context, "Wine deleted!", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(context, "Failed to delete wine: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}

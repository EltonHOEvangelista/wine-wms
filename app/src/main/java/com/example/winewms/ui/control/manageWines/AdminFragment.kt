package com.example.winewms.ui.control.manageWines

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.databinding.FragmentControlBinding
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.ResponseModel
import com.example.winewms.data.model.WarehouseModel
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.FragmentAdminBinding
import com.example.winewms.ui.control.manageWines.AdminWineAdapter
import com.example.winewms.ui.control.manageWines.OnAdminWineClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AdminFragment : Fragment() , OnAdminWineClickListener {

    lateinit var binding: FragmentAdminBinding
    private var wineApi = WineApi.retrofit.create(WineApiService::class.java)
    private lateinit var adminWineAdapter: AdminWineAdapter
    private var wineList = listOf<WineModel>()
    private lateinit var warehouses: List<WarehouseModel>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAdminBinding.inflate(inflater, container, false)

        setupClickListeners()
        setupRecyclerView()
        fetchWineAndWarehouseData()

        return binding.root
    }

    private fun setupClickListeners() {

        // Set click listener for Add Wine Button
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_adminFragment_to_addWineFragment)
        }
    }
    private fun setupRecyclerView() {
        adminWineAdapter = AdminWineAdapter(emptyList(), object : OnAdminWineClickListener {
            override fun onDeleteClick(wine: WineModel) {
                deleteWine(wine.id)
            }

            override fun onEditClick(wine: WineModel) {
                navigateToEditWineFragment(wine)
            }
        })
        binding.recyclerViewWines.layoutManager = LinearLayoutManager(context)
        binding.recyclerViewWines.adapter = adminWineAdapter
    }

    private fun fetchWineAndWarehouseData() {
        // Fetch warehouse data
        fetchWarehouseData { warehouseList ->
            // Fetch wine data
            wineApi.getWines(page = 1, limit = 10).enqueue(object : Callback<DataWrapper> {
                override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                    if (response.isSuccessful) {
                        val wineList = response.body()?.wines ?: emptyList()

                        // Aggregate stock and update UI
                        val winesWithStock = aggregateWineStock(wineList, warehouseList)
                        adminWineAdapter.updateData(winesWithStock)
                    } else {
                        Toast.makeText(context, "Failed to fetch wines", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                    Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }



    private fun updateStock(wineList: List<WineModel>, warehouses: List<WarehouseModel>): List<WineModel> {
        return wineList.map { wine ->
            wine.copy(stock = warehouses.sumOf { warehouse ->
                warehouse.aisles.sumOf { aisle ->
                    aisle.shelves.sumOf { shelf ->
                        shelf.wines.filter { it.wine_id == wine.id }.sumOf { it.stock }
                    }
                }
            })
        }
    }

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


    //function to calculate stock for each wine
    private fun aggregateWineStock(
        wineList: List<WineModel>,
        warehouses: List<WarehouseModel>
    ): List<WineModel> {
        return wineList.map { wine ->
            // Calculate the total stock for this wine across all warehouses
            val totalStock = warehouses.sumOf { warehouse ->
                warehouse.aisles.sumOf { aisle ->
                    aisle.shelves.sumOf { shelf ->
                        shelf.wines.filter { it.wine_id == wine.id }.sumOf { it.stock }
                    }
                }
            }
            wine.copy(stock = totalStock)
        }
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
        wineApi.deleteWine(wineId).enqueue(object : Callback<ResponseModel> {
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        if (it.responseStatus) { // Assuming this indicates success
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                            fetchWineAndWarehouseData() // Refresh the wine list
                        } else {
                            Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Failed to delete wine", Toast.LENGTH_SHORT).show()
                    Log.e("deleteWine", "Error: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                Log.e("deleteWine", "API call failed: ${t.message}")
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

package com.example.winewms.ui.control.reports.stock

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.databinding.FragmentStockReportBinding
import com.example.winewms.ui.control.reports.ReportsViewModel

class StockReportFragment : Fragment() {

    private lateinit var binding: FragmentStockReportBinding
    private lateinit var stockAdapter: StockReportAdapter
    private val reportsViewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockReportBinding.inflate(inflater, container, false)
        setupRecyclerView()
        setupObservers()
        setupSearch()


        reportsViewModel.fetchLowStockWines(requireContext())
        reportsViewModel.fetchStockReport(requireContext())

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.tvLowStockHeader.setOnClickListener {

            reportsViewModel.fetchLowStockWines(requireContext(), forceUpdate = true)
            updateRecyclerViewWithLowStockItems()
        }

        return binding.root
    }


    private fun updateRecyclerViewWithLowStockItems() {
        reportsViewModel.lowStockWines.observe(viewLifecycleOwner) { lowStockItems ->
            if (lowStockItems.isNotEmpty()) {
                stockAdapter.updateData(lowStockItems)
            }
        }
    }
    private fun setupObservers() {
        // Observer para a lista de estoque baixo
        reportsViewModel.lowStockWines.observe(viewLifecycleOwner) { lowStockList ->
            if (lowStockList.isNotEmpty()) {
                binding.tvLowStockHeader.visibility = View.VISIBLE
                binding.tvLowStockHeader.text = lowStockList.joinToString("\n") { "${it.name} - Only ${it.stock} left!" }
                stockAdapter.updateData(lowStockList)
            } else {
                binding.tvLowStockHeader.visibility = View.GONE
            }
        }


        reportsViewModel.stockReport.observe(viewLifecycleOwner) { stockList ->
            if (stockList.isNotEmpty()) {
                val lowStockItems = stockList.filter { it.stock < 10 }
                stockAdapter.updateData(lowStockItems)
                binding.tvLowStockHeader.visibility = if (lowStockItems.isNotEmpty()) View.VISIBLE else View.GONE
            }
        }


        reportsViewModel.filteredStockItems.observe(viewLifecycleOwner) { filteredStockList ->
            stockAdapter.updateData(filteredStockList)
            binding.tvLowStockHeader.visibility = View.GONE
        }
    }

    private fun setupSearch() {
        binding.tvLowStockHeader.setOnClickListener {
            reportsViewModel.fetchLowStockWines(requireContext())
        }
        binding.searchViewWine.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    reportsViewModel.searchStock(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    reportsViewModel.searchStock(it)
                }
                return true
            }
        })

        // Ao cancelar a pesquisa, voltar aos itens de baixo estoque
        binding.searchViewWine.setOnCloseListener {
            reportsViewModel.fetchLowStockWines(requireContext(), forceUpdate = true)
            updateRecyclerViewWithLowStockItems()
            true
        }

        // Ao clicar no alerta, voltar para a lista de itens de baixo estoque
        binding.tvLowStockHeader.setOnClickListener {
            reportsViewModel.fetchLowStockWines(requireContext(), forceUpdate = true)
            updateRecyclerViewWithLowStockItems()
        }
    }

    private fun setupRecyclerView() {
        stockAdapter = StockReportAdapter(emptyList())
        binding.rvStockReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStockReport.adapter = stockAdapter
    }
}

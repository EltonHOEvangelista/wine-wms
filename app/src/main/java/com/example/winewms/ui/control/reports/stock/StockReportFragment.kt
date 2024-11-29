package com.example.winewms.ui.control.reports.stock

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.databinding.FragmentStockReportBinding
import com.example.winewms.ui.control.reports.ReportsViewModel

class StockReportFragment : Fragment() {

    private lateinit var binding: FragmentStockReportBinding
    private val reportsViewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockReportBinding.inflate(inflater, container, false)

        setupObservers()
        reportsViewModel.fetchLowStockWines() // Chamar os vinhos com alerta
        reportsViewModel.fetchStockReport()   // Chamar os vinhos para o relatório geral

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
        return binding.root
    }

    private fun setupObservers() {
        // Observa os vinhos com estoque baixo
        reportsViewModel.lowStockWines.observe(viewLifecycleOwner) { lowStockList ->
            if (lowStockList.isNotEmpty()) {
                displayLowStockWines(lowStockList)
            } else {
                binding.tvLowStockHeader.visibility = View.GONE
            }
        }

        // Observa o relatório completo
        reportsViewModel.stockReport.observe(viewLifecycleOwner) { stockList ->
            if (stockList.isNotEmpty()) {
                updateRecyclerView(stockList)
            } else {
                Toast.makeText(requireContext(), "No stock data available", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun displayLowStockWines(lowStockList: List<StockItem>) {
        binding.tvLowStockHeader.visibility = View.VISIBLE
        val alertText =
            lowStockList.joinToString("\n") { "${it.wineName} - Only ${it.totalStock} left!" }
        binding.tvLowStockHeader.text = alertText
    }

    private fun updateRecyclerView(stockList: List<StockItem>) {
        val adapter = StockReportAdapter(stockList)
        binding.rvStockReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStockReport.adapter = adapter
    }
}

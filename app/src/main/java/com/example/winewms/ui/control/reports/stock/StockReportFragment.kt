package com.example.winewms.ui.control.reports.stock

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.databinding.FragmentStockReportBinding
import com.example.winewms.ui.control.reports.ReportsViewModel

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StockReportFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StockReportFragment : Fragment() {

    private lateinit var binding: FragmentStockReportBinding
    private val reportsViewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStockReportBinding.inflate(inflater, container, false)
        setupObservers()
        reportsViewModel.fetchStockReport()
        return binding.root
    }

    private fun setupObservers() {
        reportsViewModel.stockReport.observe(viewLifecycleOwner) { stockList ->
            if (stockList.isNotEmpty()) {
                val adapter = StockReportAdapter(stockList)
                binding.rvStockReport.layoutManager = LinearLayoutManager(requireContext())
                binding.rvStockReport.adapter = adapter
            } else {
                Toast.makeText(requireContext(), "No stock data available", Toast.LENGTH_SHORT).show()
            }
        }

        reportsViewModel.stockReport.observe(viewLifecycleOwner) { stockItems ->
            if (stockItems != null && stockItems.isNotEmpty()) {
                updateRecyclerView(stockItems)
            } else {
                Toast.makeText(requireContext(), "No stock data available", Toast.LENGTH_SHORT).show()
            }

        }

    }

    private fun updateRecyclerView(stockItems: List<StockItem>) {
        val adapter = StockReportAdapter(stockItems)
        binding.rvStockReport.adapter = adapter
        binding.rvStockReport.layoutManager = LinearLayoutManager(requireContext())
    }

}

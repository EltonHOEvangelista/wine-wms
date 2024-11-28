package com.example.winewms.ui.control.reports

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.winewms.R

import com.example.winewms.databinding.FragmentReportsBinding

class ReportsFragment : Fragment() {

    private lateinit var binding: FragmentReportsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentReportsBinding.inflate(inflater, container, false)

        setupClickListeners()
        setupBackButton()
        return binding.root
    }

    private fun setupBackButton() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupClickListeners() {

        //navigate to sales report
        binding.btnSalesReport.setOnClickListener {
            findNavController().navigate(R.id.action_reportsFragment_to_salesReportFragment)
        }

       //navigate to stock report
        binding.btnStockReport.setOnClickListener {
            findNavController().navigate(R.id.action_reportsFragment_to_stockReportFragment)
        }

        //navigate to financial report
        binding.btnFinancialReport.setOnClickListener {
           // findNavController().navigate(R.id.action_reportsFragment_to_financialReportFragment)
        }
    }
}

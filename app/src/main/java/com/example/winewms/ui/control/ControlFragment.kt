package com.example.winewms.ui.control

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.databinding.FragmentControlBinding
import com.example.winewms.ui.control.reports.ReportsViewModel
import com.example.winewms.ui.control.reports.sales.SalesComparison
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.data.RadarData
import com.github.mikephil.charting.data.RadarDataSet
import com.github.mikephil.charting.data.RadarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter


class ControlFragment : Fragment() {

    private lateinit var binding: FragmentControlBinding
    private val reportsViewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentControlBinding.inflate(inflater, container, false)

        setupClickListeners()
        setupObservers()

        reportsViewModel.fetchSalesComparison()

        return binding.root
    }

    private fun setupClickListeners() {


        //Configure click on "Manage Wines" button
        binding.btnManageWines.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_control_to_adminFragment)
        }

        // Configure click on "Reports" button
        binding.btnReports.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_control_to_reportsFragment)
        }
    }

    private fun setupObservers() {
        reportsViewModel.salesComparison.observe(viewLifecycleOwner) { salesComparison ->
            if (salesComparison != null) {
                updateLineChart(salesComparison)
            } else {
                Toast.makeText(requireContext(), "Failed to load sales data", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun updateLineChart(salesComparison: SalesComparison) {

        val previousMonthEntries = listOf(
            Entry(0f, salesComparison.previousMonthSales * 0.8f),
            Entry(1f, salesComparison.previousMonthSales)
        )
        val previousMonthDataSet = LineDataSet(previousMonthEntries, "Previous Month").apply {
            color = ContextCompat.getColor(requireContext(), R.color.LightRedWine)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.LightRedWine))
        }

        val currentMonthEntries = listOf(
            Entry(0f, salesComparison.currentMonthSales * 0.9f),
            Entry(2f, salesComparison.currentMonthSales)
        )
        val currentMonthDataSet = LineDataSet(currentMonthEntries, "Current Month").apply {
            color = ContextCompat.getColor(requireContext(), R.color.DarkRedWine)
            valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.DarkRedWine))
        }

        val lineData = LineData(previousMonthDataSet, currentMonthDataSet)
        binding.lineChart.data = lineData

        val xAxis = binding.lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(listOf("Previous Month", "Current Month"))
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            textColor = ContextCompat.getColor(requireContext(), R.color.black)
        }

        val maxSales = maxOf(salesComparison.previousMonthSales, salesComparison.currentMonthSales)
        val quartile = maxSales / 4f

        binding.lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = maxSales * 1.2f
            granularity = quartile
            textColor = ContextCompat.getColor(requireContext(), R.color.black)
        }
        binding.lineChart.axisRight.isEnabled = false

        binding.lineChart.description.apply {
            text = "Monthly Sales Comparison: Previous vs Current Month"
            textColor = ContextCompat.getColor(requireContext(), R.color.black)
            textSize = 12f
        }

        binding.lineChart.apply {
            setDrawGridBackground(false)
            legend.isEnabled = true
            legend.textColor = ContextCompat.getColor(requireContext(), R.color.black)
            animateY(1000)
            invalidate()
        }
    }
}






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
                updateLineChartTests(salesComparison)
            } else {
                Toast.makeText(requireContext(), "Failed to load sales data", Toast.LENGTH_SHORT).show()
            }
        }
    }

//for mockdata use this code
    private fun updateLineChartTests(salesComparison: SalesComparison) {
        // Simular entradas para o mês anterior
        val previousMonthEntries = listOf(
            Entry(0f, salesComparison.previousMonthSales * 0.8f), // Exemplo de valor inicial
            Entry(1f, salesComparison.previousMonthSales)        // Valor real do mês anterior
        )
        val previousMonthDataSet = LineDataSet(previousMonthEntries, "Previous Month")
        previousMonthDataSet.color = ContextCompat.getColor(requireContext(), R.color.LightRedWine) // Cor da linha
        previousMonthDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        previousMonthDataSet.lineWidth = 2f
        previousMonthDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.LightRedWine)) // Cor dos pontos

        // Simular entradas para o mês atual
        val currentMonthEntries = listOf(
            Entry(0f, salesComparison.currentMonthSales * 0.9f), // Exemplo de valor inicial
            Entry(2f, salesComparison.currentMonthSales)        // Valor real do mês atual
        )
        val currentMonthDataSet = LineDataSet(currentMonthEntries, "Current Month")
        currentMonthDataSet.color = ContextCompat.getColor(requireContext(), R.color.DarkRedWine) // Cor da linha
        currentMonthDataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black)
        currentMonthDataSet.lineWidth = 2f
        currentMonthDataSet.setCircleColor(ContextCompat.getColor(requireContext(), R.color.DarkRedWine)) // Cor dos pontos

        // Configurando o gráfico com os dois conjuntos de dados
        val lineData = LineData(previousMonthDataSet, currentMonthDataSet)
        binding.lineChart.data = lineData

        // Configuração do gráfico
        val xAxis = binding.lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Previous Month", "Current Month"))
        xAxis.granularity = 1f
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)

        binding.lineChart.description.text = "Monthly Sales Comparison"
        binding.lineChart.description.textColor = ContextCompat.getColor(requireContext(), R.color.black)
        binding.lineChart.animateY(1000)
        binding.lineChart.invalidate() // Atualiza o gráfico
    }

//for Real data from backend use this code
//private fun updateLineChart(salesComparison: SalesComparison) {
//    // Retrieve the actual sales data from the ViewModel
//    val currentMonthSales = salesComparison.currentMonthSales
//    val previousMonthSales = salesComparison.previousMonthSales
//
//    // Create data entries for the previous month
//    val previousMonthEntries = listOf(
//        Entry(0f, previousMonthSales * 0.9f), // Simulates an intermediate value
//        Entry(1f, previousMonthSales)        // Actual value for the previous month
//    )
//
//    // Create data entries for the current month
//    val currentMonthEntries = listOf(
//        Entry(1f, previousMonthSales),       // Starts where the previous month ended
//        Entry(2f, currentMonthSales)         // Actual value for the current month
//    )
//
//    // Configure the dataset for the previous month
//    val previousMonthDataSet = LineDataSet(previousMonthEntries, "Previous Month").apply {
//        color = ContextCompat.getColor(requireContext(), R.color.lightRed) // Line color
//        valueTextColor = ContextCompat.getColor(requireContext(), R.color.primaryText) // Text color for values
//        lineWidth = 2f // Line thickness
//        setCircleColor(ContextCompat.getColor(requireContext(), R.color.lightRed)) // Circle color
//    }
//
//    // Configure the dataset for the current month
//    val currentMonthDataSet = LineDataSet(currentMonthEntries, "Current Month").apply {
//        color = ContextCompat.getColor(requireContext(), R.color.primaryDark) // Line color
//        valueTextColor = ContextCompat.getColor(requireContext(), R.color.primaryText) // Text color for values
//        lineWidth = 2f // Line thickness
//        setCircleColor(ContextCompat.getColor(requireContext(), R.color.primaryDark)) // Circle color
//    }
//
//    // Add both datasets to the chart data
//    val lineData = LineData(previousMonthDataSet, currentMonthDataSet)
//    binding.lineChart.data = lineData
//
//    // Additional chart configurations
//    binding.lineChart.apply {
//        description.text = "Monthly Sales Comparison" // Chart description
//        description.textColor = ContextCompat.getColor(requireContext(), R.color.primaryText) // Description text color
//
//        // Configure the X-axis
//        xAxis.apply {
//            valueFormatter = IndexAxisValueFormatter(listOf("Previous Month", "Current Month")) // X-axis labels
//            granularity = 1f // Distance between values
//            position = XAxis.XAxisPosition.BOTTOM // Position at the bottom of the chart
//            textColor = ContextCompat.getColor(requireContext(), R.color.primaryText) // X-axis text color
//        }
//
//        // Configure the Y-axis (left axis)
//        axisLeft.apply {
//            val maxSales = maxOf(currentMonthSales, previousMonthSales) // Determine the highest sales value
//            axisMinimum = 0f // Start Y-axis from 0
//            axisMaximum = maxSales * 1.2f // Extend Y-axis 20% beyond the highest value
//            textColor = ContextCompat.getColor(requireContext(), R.color.primaryText) // Y-axis text color
//        }
//
//        // Disable the right Y-axis (optional)
//        axisRight.isEnabled = false
//
//        // Add animation for better visualization
//        animateY(1000) // Animate Y-axis over 1 second
//        invalidate() // Redraw the chart
//    }
//}


}


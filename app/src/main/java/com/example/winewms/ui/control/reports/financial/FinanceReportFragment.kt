package com.example.winewms.ui.control.reports.financial

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.winewms.R
import com.example.winewms.databinding.FragmentFinanceReportBinding
import com.example.winewms.ui.control.reports.ReportsViewModel
import com.google.android.material.snackbar.Snackbar
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class FinanceReportFragment : Fragment() {

    private lateinit var binding: FragmentFinanceReportBinding
    private val reportsViewModel: ReportsViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFinanceReportBinding.inflate(inflater, container, false)
        setupObservers()
        setupFilterButton()
        fetchCurrentMonthData()

        val currentMonth = getCurrentMonth()
        binding.tvCurrentMonth.text = "Current Month: $currentMonth"

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }


        return binding.root
    }

    private fun setupObservers() {
        reportsViewModel.financeReport.observe(viewLifecycleOwner) { report ->
            if (report == null) {
                Snackbar.make(binding.root, "Failed to load report. Please try again.", Snackbar.LENGTH_LONG).show()
                clearUI()
            } else {
                updateUIWithFinanceData(report)
            }
        }
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val dialogView =
            LayoutInflater.from(requireContext()).inflate(R.layout.dialog_date_filter, null)
        val startDateInput = dialogView.findViewById<EditText>(R.id.etStartDate)
        val endDateInput = dialogView.findViewById<EditText>(R.id.etEndDate)

        startDateInput.setOnClickListener {
            showDatePicker { date -> startDateInput.setText(date) }
        }

        endDateInput.setOnClickListener {
            showDatePicker { date -> endDateInput.setText(date) }
        }

        AlertDialog.Builder(requireContext())
            .setTitle("Filter Dates")
            .setView(dialogView)
            .setPositiveButton("Apply") { _, _ ->
                val startDate = startDateInput.text.toString()
                val endDate = endDateInput.text.toString()

                if (startDate.isNotBlank() && endDate.isNotBlank()) {
                    applyFilters(startDate, endDate)
                } else {
                    Toast.makeText(requireContext(), "Invalid dates", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDatePicker(onDateSelected: (String) -> Unit) {
        val datePicker = com.google.android.material.datepicker.MaterialDatePicker.Builder.datePicker()
            .setTitleText("Select a date")
            .build()

        datePicker.show(parentFragmentManager, "datePicker")

        datePicker.addOnPositiveButtonClickListener { selection ->
            val date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(selection)
            onDateSelected(date)
        }
    }

    private fun fetchCurrentMonthData() {
        val calendar = Calendar.getInstance()

        // Defina o primeiro dia do mês
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        val startDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Defina o último dia do mês
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        val endDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(calendar.time)

        // Chame fetchFinanceReport com os valores calculados
        reportsViewModel.fetchFinanceReport(requireContext(), startDate, endDate)
    }

    private fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        return monthFormat.format(calendar.time)
    }

    private fun applyFilters(startDate: String, endDate: String) {
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please select both start and end dates.", Toast.LENGTH_SHORT).show()
            return
        }
        reportsViewModel.fetchFinanceReport(requireContext(), startDate, endDate)
    }

    private fun updateUIWithFinanceData(report: FinanceReportModel) {
        binding.tvTotalSales.text = getString(R.string.total_sales, String.format("%.2f", report.totalSales))
        binding.tvTotalPurchases.text = getString(R.string.total_purchases, String.format("%.2f", report.totalPurchases))
        binding.tvTotalBalance.text = getString(R.string.total_balance, String.format("%.2f", report.totalBalance))

        val balanceColor = if (report.totalBalance < 0) R.color.DarkRedWine else R.color.black
        binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), balanceColor))
    }

    private fun clearUI() {
        binding.tvTotalSales.text = getString(R.string.total_sales, "0.00")
        binding.tvTotalPurchases.text = getString(R.string.total_purchases, "0.00")
        binding.tvTotalBalance.text = getString(R.string.total_balance, "0.00")
        binding.tvTotalBalance.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
    }

}


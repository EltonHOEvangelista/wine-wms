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
        reportsViewModel.financeReport.observe(viewLifecycleOwner) { report: FinanceReportModel? ->
            if (report == null) {
                Snackbar.make(binding.root, "Failed to load report. Please try again.", Snackbar.LENGTH_LONG).show()

                binding.tvTotalSales.text = getString(R.string.total_sales, "0.00")
                binding.tvTotalPurchases.text = getString(R.string.total_purchases, "0.00")
                binding.tvTotalBalance.text = getString(R.string.total_balance, "0.00")
                binding.tvTotalBalance.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            } else {
                val totalSales = report.totalSales
                val totalPurchases = report.totalPurchases
                val totalBalance = report.totalBalance

                binding.tvTotalSales.text =
                    getString(R.string.total_sales, String.format("%.2f", totalSales))
                binding.tvTotalPurchases.text =
                    getString(R.string.total_purchases, String.format("%.2f", totalPurchases))
                binding.tvTotalBalance.text =
                    getString(R.string.total_balance, String.format("%.2f", totalBalance))

                val balanceColor =
                    if (totalBalance < 0) R.color.DarkRedWine else R.color.black
                binding.tvTotalBalance.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        balanceColor
                    )
                )
            }
        }
    }

    private fun setupFilterButton() {
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
    }

    private fun showFilterDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_date_filter, null)
        val startDateInput = dialogView.findViewById<EditText>(R.id.etStartDate)
        val endDateInput = dialogView.findViewById<EditText>(R.id.etEndDate)

        // Set click listeners to open MaterialDatePicker
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
                    reportsViewModel.fetchFinanceReport(startDate, endDate)
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
        reportsViewModel.fetchCurrentMonthFinanceReport()
    }

    private fun getCurrentMonth(): String {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        return monthFormat.format(calendar.time)
    }
}


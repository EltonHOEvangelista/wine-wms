package com.example.winewms.ui.control.reports.sales

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.databinding.FragmentSalesReportBinding
import com.example.winewms.ui.control.reports.ReportsViewModel
import com.google.android.material.datepicker.MaterialDatePicker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SalesReportFragment : Fragment() {

    private lateinit var binding: FragmentSalesReportBinding
    private val reportsViewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSalesReportBinding.inflate(inflater, container, false)
        setupObservers()
        setupFilters()
        return binding.root
    }

    private fun setupObservers() {
        reportsViewModel.salesReport.observe(viewLifecycleOwner) { report ->
            if (report != null) {
                updateRecyclerView(report)
            } else {
                Toast.makeText(requireContext(), "Failed to load sales report", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFilters() {
        // Date pickers
        binding.btnStartDate.setOnClickListener { showDatePickerDialog { date -> binding.btnStartDate.text = date } }
        binding.btnEndDate.setOnClickListener { showDatePickerDialog { date -> binding.btnEndDate.text = date } }

        // Handle "All" checkbox behavior
        binding.filterOptionAll.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Uncheck other category filters when "All" is selected
                binding.filterOptionRed.isChecked = false
                binding.filterOptionWhite.isChecked = false
                binding.filterOptionRose.isChecked = false
                binding.filterOptionSparkling.isChecked = false
            }
        }

        // Handle behavior for other category checkboxes
        listOf(
            binding.filterOptionRed,
            binding.filterOptionWhite,
            binding.filterOptionRose,
            binding.filterOptionSparkling
        ).forEach { checkBox ->
            checkBox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Uncheck "All" if any other category is selected
                    binding.filterOptionAll.isChecked = false
                }
            }
        }

        // Apply filters button
        binding.btnApplyFilters.setOnClickListener {
            applyFilters()
        }
    }

    private fun applyFilters() {
        val startDate = binding.btnStartDate.text.toString()
        val endDate = binding.btnEndDate.text.toString()
        val categories = getSelectedCategories()
        val bestSellers = binding.filterOptionBestSellers.isChecked

        // Validate dates
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please select both start and end dates.", Toast.LENGTH_SHORT).show()
            return
        }

        // Trigger fetching of the sales report with filters
        reportsViewModel.fetchSalesReportWithFilters(
            startDate = startDate,
            endDate = endDate,
            categories = categories,
            bestSellers = bestSellers,

        )
    }

    private fun getSelectedCategories(): List<String> {
        val selectedCategories = mutableListOf<String>()
        if (binding.filterOptionAll.isChecked) {
            return emptyList()
        }

        if (binding.filterOptionRed.isChecked) selectedCategories.add("Red Wine")
        if (binding.filterOptionWhite.isChecked) selectedCategories.add("White Wine")
        if (binding.filterOptionRose.isChecked) selectedCategories.add("Rose Wine")
        if (binding.filterOptionSparkling.isChecked) selectedCategories.add("Sparkling Wine")

        return selectedCategories
    }

    private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
        val datePicker = MaterialDatePicker.Builder.datePicker().build()
        datePicker.show(parentFragmentManager, "datePicker")
        datePicker.addOnPositiveButtonClickListener {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it))
            onDateSelected(date)
        }
    }

    private fun updateRecyclerView(report: SalesReportModel) {
        val adapter = SalesReportAdapter(report)
        binding.rvSalesReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSalesReport.adapter = adapter
    }
}




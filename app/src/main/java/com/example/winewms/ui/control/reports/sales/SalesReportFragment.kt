package com.example.winewms.ui.control.reports.sales

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.databinding.FragmentSalesReportBinding
import com.example.winewms.ui.control.reports.ReportsViewModel
import androidx.appcompat.widget.SearchView

class SalesReportFragment : Fragment() {

    private lateinit var binding: FragmentSalesReportBinding
    private val reportsViewModel: ReportsViewModel by viewModels()
    private lateinit var adapter: SalesReportAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSalesReportBinding.inflate(inflater, container, false)

        // Initialize RecyclerView, observers, filters, and search setup
        setupRecyclerView()
        setupObservers()
        setupFilters()
        setupSearch()

        binding.btnBack.setOnClickListener {
            // Chama o método para voltar à tela anterior
            handleBackButton()
        }

        // Fetch the initial list of sold wines
        reportsViewModel.fetchSoldWines()

        return binding.root
    }

    private fun handleBackButton() {
        val fragmentManager = parentFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {

            requireActivity().finish()
        }
    }

    private fun setupRecyclerView() {
        // Initialize the adapter with an empty list and set a click listener for wine items
        adapter = SalesReportAdapter(
            soldWines = emptyList(),
            onItemClick = { wine -> showWineDetailsDialog(wine) } // Opens popup when a wine is clicked
        )
        // Set up the RecyclerView with a linear layout manager
        binding.rvSalesReport.layoutManager = LinearLayoutManager(requireContext())
        binding.rvSalesReport.adapter = adapter
    }

    private fun setupObservers() {
        // Observe changes in the list of sold wines
        reportsViewModel.soldWines.observe(viewLifecycleOwner) { soldWines ->
            if (soldWines.isNotEmpty()) {
                // Update the adapter with the new list of sold wines
                adapter.updateData(soldWines)
            } else {
                // Show a message if no wines are available
                Toast.makeText(requireContext(), "No wines sold available.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupFilters() {
        // Set up the filter icon click listener to show the filter popup
        binding.imgFilter.setOnClickListener {
            showFilterPopup()
        }
    }

    private fun setupSearch() {
        // Set up SearchView to filter wines by name
        binding.svSearchWine.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    reportsViewModel.searchWine(it) // Implement `searchWine` in ViewModel
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Optional: Handle real-time search (if needed)
                return false
            }
        })
    }

    private fun showFilterPopup() {
        // Display the filter popup fragment
        val dialog = FilterDialogFragment()
        dialog.setOnApplyFiltersListener { startDate, endDate, categories, bestSellers ->
            applyFilters(startDate, endDate, categories, bestSellers)
        }
        dialog.show(parentFragmentManager, "FilterDialog")
    }

    private fun applyFilters(startDate: String, endDate: String, categories: List<String>, bestSellers: Boolean) {
        // Validate that both start and end dates are provided
        if (startDate.isEmpty() || endDate.isEmpty()) {
            Toast.makeText(requireContext(), "Please select both start and end dates.", Toast.LENGTH_SHORT).show()
            return
        }

        // Fetch the sales report with the specified filters
        reportsViewModel.fetchSalesReportWithFilters(
            startDate = startDate,
            endDate = endDate,
            categories = categories,
            bestSellers = bestSellers
        )
    }

    private fun showWineDetailsDialog(wine: SoldWine) {
        // Create a custom view for the dialog
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_wine_details, null)

        // Populate the dialog with wine details
        dialogView.findViewById<TextView>(R.id.tvWineName).text = wine.wineName
        dialogView.findViewById<TextView>(R.id.tvQuantitySold).text = "Sold: ${wine.quantitySold}"

        // Build the AlertDialog
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(true)
            .setPositiveButton("Manage Wine") { _, _ ->
                // Navigate to the admin fragment or perform some action
                // findNavController().navigate(R.id.action_salesReportFragment_to_adminFragment)
            }
            .setNegativeButton("Back", null) // Closes the dialog
            .create()

        // Show the dialog
        dialog.show()
    }
}



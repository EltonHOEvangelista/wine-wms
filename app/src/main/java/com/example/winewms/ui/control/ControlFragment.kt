package com.example.winewms.ui.control

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.databinding.FragmentControlBinding

class ControlFragment : Fragment() {

    private lateinit var binding: FragmentControlBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentControlBinding.inflate(inflater, container, false)

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {

         //Configure click on "Product Management" button
        binding.btnProductManagement.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_control_to_adminFragment)
        }

        //Configure click on "Warehouse Management" button
        binding.btnWarehouseManagement.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_control_to_warehouseFragment)
        }

        // Configure click on "Reports" button
        binding.btnFinancialManagement.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_control_to_financialFragment)
        }

        // Configure click on "Reports" button
        binding.btnSalesManagement.setOnClickListener {

        }
    }
}


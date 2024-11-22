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

    lateinit var binding: FragmentControlBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentControlBinding.inflate(inflater)

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {

        // Set click listener for Add Wine Button
        binding.floatingActionButton.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_control_to_addWineFragment)
        }
    }

}
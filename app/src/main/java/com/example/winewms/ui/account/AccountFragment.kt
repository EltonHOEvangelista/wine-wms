package com.example.winewms.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.databinding.FragmentAccountBinding

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAccountBinding.inflate(inflater)

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {

        //set click listener on linear Layout Account Detail to show Account Form
        binding.linearLayoutAccountDetail.setOnClickListener() {
            displayAccountForm()
        }

        // Set click listener for Sign In
        binding.imgSignin.setOnClickListener {
            findNavController().navigate(R.id.navigation_signin)
        }
    }

    private fun displayAccountForm() {
        if (binding.linearLayoutAccountForm.visibility == View.GONE) {
            binding.linearLayoutAccountForm.visibility = View.VISIBLE
            binding.imgArrowAccountDetails.visibility = View.GONE
        }
        else {
            binding.linearLayoutAccountForm.visibility = View.GONE
            binding.imgArrowAccountDetails.visibility = View.VISIBLE
        }
    }
}
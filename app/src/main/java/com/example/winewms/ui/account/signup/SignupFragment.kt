package com.example.winewms.ui.account.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.databinding.FragmentSigninBinding
import com.example.winewms.databinding.FragmentSignupBinding

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignupBinding.inflate(inflater)

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.btnSignup.setOnClickListener{
            findNavController().navigate(R.id.action_navigation_signup_to_navigation_account)
        }
    }
}
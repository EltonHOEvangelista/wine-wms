package com.example.winewms.ui.account.signin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.databinding.FragmentAccountBinding
import com.example.winewms.databinding.FragmentSigninBinding
import com.google.android.material.bottomnavigation.BottomNavigationView

class SigninFragment : Fragment() {

    private lateinit var binding: FragmentSigninBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSigninBinding.inflate(inflater)

        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {

        binding.btnSignin.setOnClickListener{

            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
            bottomNavigationView.menu.findItem(R.id.navigation_control)?.isVisible = true
            bottomNavigationView.menu.findItem(R.id.navigation_control)?.isEnabled = true

            findNavController().navigate(R.id.navigation_account)
        }

        binding.btnGoToSignup.setOnClickListener{
            findNavController().navigate(R.id.navigation_signup)
        }
    }
}
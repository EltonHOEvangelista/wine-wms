package com.example.winewms.ui.account.signin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.data.sql.DatabaseHelper
import com.example.winewms.databinding.FragmentSigninBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SigninFragment : Fragment() {

    private lateinit var binding: FragmentSigninBinding
    private val apiService = WineApi.service
    private val dbHelper by lazy {
        DatabaseHelper(requireContext(), "wine_wms.db")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSigninBinding.inflate(inflater, container, false)
        setupClickListeners()
        return binding.root
    }

    private fun setupClickListeners() {
        // Listener to sign in the user
        binding.btnSignin.setOnClickListener {
            val email = binding.txtEmail.text.toString().trim()
            val password = binding.txtPassword.text.toString().trim()

            // Verify login locally (SQLite)
            val localUser = dbHelper.getAccount(email)

            if (localUser != null && localUser.password == password) {
                Toast.makeText(requireContext(), "Login successful (local)!", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.navigation_home) // Navega para a página home
            } else {
                // Verify login remotely (API)
                val user = SigninModel(email = email, password = password)
                apiService.signin(user).enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            Toast.makeText(requireContext(), "Login successful (backend)!", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.navigation_home) // Navega para a página home
                            //findNavController().navigate(R.id.action_navigation_signin_to_navigation_account)

//            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
//            bottomNavigationView.menu.findItem(R.id.navigation_control)?.isVisible = true
//            bottomNavigationView.menu.findItem(R.id.navigation_control)?.isEnabled = true


//            local database update missing. Fetch data from backend and send it to SQL Lite.

                        } else {
                            Toast.makeText(requireContext(), "Login failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        // Listener to navigate to the Sign Up page
        binding.btnGoToSignup.setOnClickListener {
            findNavController().navigate(R.id.action_nav_signin_to_nav_signup)
        }

        // Listener to navigate to the Password Recovery page
        binding.txtRecoverPassword.setOnClickListener {
            Toast.makeText(requireContext(), "Password recovery feature coming soon!", Toast.LENGTH_SHORT).show()
        }
    }
}


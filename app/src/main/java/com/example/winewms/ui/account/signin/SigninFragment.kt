package com.example.winewms.ui.account.signin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.sql.DatabaseHelper
import com.example.winewms.databinding.FragmentSigninBinding
import com.example.winewms.ui.account.AccountDataWrapper
import com.example.winewms.ui.account.AccountModel
import com.example.winewms.ui.account.AccountViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SigninFragment : Fragment() {

    private lateinit var binding: FragmentSigninBinding

    lateinit var accountModel: AccountModel
    private val accountViewModel: AccountViewModel by activityViewModels()

    //Instantiate Wine Api
    var wineApi = WineApi.retrofit.create(WineApiService::class.java)

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

            if (email.isNotEmpty() && password.isNotEmpty()) {
                val signinModel = SigninModel(
                    email = email,
                    password = password
                )
                // call function to signin on backend
                signinServer(signinModel)

            } else {
                Toast.makeText(requireContext(), "Please, enter your email and password", Toast.LENGTH_SHORT).show()
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

    //Function to signin on server/backend
    private fun signinServer(signinModel: SigninModel) {
        //Fetch data from api
        val apiCall = wineApi.signin(signinModel)

        //Asynchronous call to create new account
        apiCall.enqueue(object : Callback<AccountDataWrapper> {
            override fun onFailure(call: Call<AccountDataWrapper>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<AccountDataWrapper>, response: Response<AccountDataWrapper>) {
                if (response.isSuccessful) {
                    val dataWrapper = response.body()
                    if (dataWrapper != null) {
                        if (dataWrapper.requestStatus) {
                            accountModel = dataWrapper.accountModel
                            Toast.makeText(requireContext(), dataWrapper.message, Toast.LENGTH_SHORT).show()

                            //Signin locally
                            signinApp(accountModel)
                        } else {
                            Toast.makeText(requireContext(), "Login fail: ${dataWrapper.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Login fail: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                    }
                }
                else {
                    Toast.makeText(requireContext(), "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun signinApp(accountModel: AccountModel) {
        val dbHelper by lazy {
            DatabaseHelper(requireContext())
        }

        val success = dbHelper.signin(accountModel)
        if (success) {
            // Store fetched data in the ViewModel
            accountViewModel.setAccount(accountModel)

            Toast.makeText(requireContext(), "Welcome to Wine Warehouse, ${accountModel.firstName}!", Toast.LENGTH_SHORT).show()

            //Activate UI admin features. Type = 1 (administrator)
            if (accountModel.type == 1) {
                val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                navView.menu.findItem(R.id.navigation_control)?.isVisible = true
                navView.menu.findItem(R.id.navigation_control)?.isEnabled = true
            }

            // Navigate to the SignIn page
            findNavController().navigate(R.id.navigation_home)
            //findNavController().navigate(R.id.action_navigation_signup_to_navigation_account)
        } else {
            Toast.makeText(requireContext(), "Fail to login. Please try again.", Toast.LENGTH_SHORT).show()
        }
        dbHelper.close()
    }
}


package com.example.winewms.ui.account.signup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import com.example.winewms.R
import com.example.winewms.databinding.FragmentSignupBinding
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.sql.DatabaseHelper
import com.example.winewms.ui.account.AccountAddressModel
import com.example.winewms.ui.account.AccountDataWrapper
import com.example.winewms.ui.account.AccountModel
import com.example.winewms.ui.account.AccountViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding

    lateinit var accountModel: AccountModel
    private val accountViewModel: AccountViewModel by activityViewModels()

    //Instantiate Wine Api
    var wineApi = WineApi.retrofit.create(WineApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        //hide action bar
        (activity as AppCompatActivity).supportActionBar?.hide()

        //set click listener on Create Account Button
        binding.btnSignup.setOnClickListener { createAccount() }

        // Toggle address expand
        binding.toggleAddress.setOnClickListener {
            if (binding.addressLayout.visibility == View.GONE) {
              //  expandView(binding.addressLayout)
                binding.toggleAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0)
            } else {
                //collapseView(binding.addressLayout)
                binding.toggleAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_more, 0)
            }
        }
        return binding.root
    }

    private fun createAccount() {
        // Get user input from text fields
        val firstName = binding.txtFirstName.text.toString().trim()
        val lastName = binding.txtLastName.text.toString().trim()
        val email = binding.txtEmail.text.toString().trim()
        val password = binding.txtPassword.text.toString().trim()
        val confirmPassword = binding.txtConfirmPassword.text.toString().trim()
        val phone = binding.txtPhone.text.toString().trim()

        // Get address details entered by the user
        val address = binding.txtAddress.text.toString().trim()
        val city = binding.txtCity.text.toString().trim()
        val province = binding.txtProvince.text.toString().trim()
        val postalCode = binding.txtPostalCode.text.toString().trim()
        val country = binding.txtAccountCountry.text.toString().trim()

        // Check if required fields are filled out
        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Please fill out all required fields!", Toast.LENGTH_SHORT).show()
            return
        }

        // Ensure the passwords match
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match!", Toast.LENGTH_SHORT).show()
            return
        }

        val accountAddress = if (address.isNotEmpty() || city.isNotEmpty() || province.isNotEmpty() || postalCode.isNotEmpty()) {
            AccountAddressModel(
                address = address,
                city = city,
                province = province,
                postalCode = postalCode,
                country = country
            )
        } else {
            null
        }

        // Create the account model with all input data
        val newAccount = AccountModel(
            accountId = "0", // It will be replaced by backend account id
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            phone = phone,
            status = 1, // Default status as active
            // Account Type: 0 (customer); 1 (admin)
            // In case of first account, type will be set as admin by the backend.
            type = 0,
            address = accountAddress!!
        )

        //Fetch data from api
        val apiCall = wineApi.signup(newAccount)

        //Asynchronous call to create new account
        apiCall.enqueue(object : Callback<AccountDataWrapper> {
            override fun onFailure(call: Call<AccountDataWrapper>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<AccountDataWrapper>, response: Response<AccountDataWrapper>
            ) {
                if (response.isSuccessful) {
                    val dataWrapper = response.body()
                    if (dataWrapper != null) {
                        if (dataWrapper.requestStatus) {
                            accountModel = dataWrapper.accountModel
                            Toast.makeText(requireContext(), dataWrapper.message, Toast.LENGTH_SHORT).show()

                            // Store fetched data in the ViewModel
                            accountViewModel.setAccount(accountModel)

                            //create local account on app (SQL Lite database) and signin
                            signupAndSigninLocally(accountModel)
                        } else {
                            Toast.makeText(requireContext(), "Create account failed: ${dataWrapper.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Create account failed: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }

    //Function to create local account on app (SQL Lite database) and signin
    private fun signupAndSigninLocally(accountModel: AccountModel) {
        val dbHelper by lazy {
            DatabaseHelper(requireContext())
        }

        if (dbHelper.createAccount(accountModel) && dbHelper.signin(accountModel)) {

            Toast.makeText(requireContext(), "Welcome to Wine Warehouse, ${accountModel.firstName}!", Toast.LENGTH_SHORT).show()

            //Activate UI admin features. Type = 1 (administrator)
            if (accountModel.type == 1) {
                val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                navView.menu.findItem(R.id.navigation_control)?.isVisible = true
                navView.menu.findItem(R.id.navigation_control)?.isEnabled = true
            }

            findNavController().navigate(R.id.navigation_home)
            //findNavController().navigate(R.id.action_navigation_signup_to_navigation_account)
        } else {
            Toast.makeText(requireContext(), "Failed to create local account. Please try again.", Toast.LENGTH_SHORT).show()
        }
        dbHelper.close()
    }
}

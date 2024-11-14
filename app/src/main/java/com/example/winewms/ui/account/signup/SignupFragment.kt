package com.example.winewms.ui.account.signup

import android.animation.ObjectAnimator
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.doOnEnd
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.databinding.FragmentSigninBinding
import com.example.winewms.databinding.FragmentSignupBinding
import com.example.winewms.api.WineApi
import com.example.winewms.data.sql.DatabaseHelper
import com.example.winewms.ui.account.AccountAddressModel
import com.example.winewms.ui.account.AccountModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignupFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private val dbHelper by lazy {
        DatabaseHelper(requireContext(), "wine_wms.db")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentSignupBinding.inflate(inflater, container, false)

        (activity as AppCompatActivity).supportActionBar?.hide()

        binding.btnSignup.setOnClickListener { createAccount() }

        // Toggle for address expand/collapse
        // Toggle address expand/collapse com animação
        binding.toggleAddress.setOnClickListener {
            if (binding.addressLayout.visibility == View.GONE) {
                // Expandir o layout de endereço
              //  expandView(binding.addressLayout)
                binding.toggleAddress.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_expand_less, 0)
            } else {
                // Recolher o layout de endereço
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

        val userAddress = if (address.isNotEmpty() || city.isNotEmpty() || province.isNotEmpty() || postalCode.isNotEmpty()) {
            AccountAddressModel(
                address = address,
                city = city,
                province = province,
                postalCode = postalCode
            )
        } else {
            null
        }

        // Create the account model with all input data
        val user = AccountModel(
            accountId = 0, // Initial ID for a new user
            firstName = firstName,
            lastName = lastName,
            email = email,
            password = password,
            confirmPassword = confirmPassword,
            phone = phone,
            accountStatus = 1, // Default status as active
            accountType = 0, // Default user type
            address = userAddress
        )

        // Save the user locally in SQLite
        val success = dbHelper.createAccount(user)
        if (success) {
            Toast.makeText(requireContext(), "Account created successfully!", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.navigation_signin)
            //findNavController().navigate(R.id.action_navigation_signup_to_navigation_account)
            Toast.makeText(requireContext(), "Please, Sign in with your credentials!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show()
        }

        // Save the user locally in SQLite
//        val localSaveSuccess = dbHelper.createAccount(user)
//        if (localSaveSuccess) {
//            Toast.makeText(requireContext(), "User saved locally!", Toast.LENGTH_SHORT).show()
//        } else {
//            Toast.makeText(requireContext(), "Failed to save user locally.", Toast.LENGTH_SHORT)
//                .show()
//            return
//        }

        // Send the user data to the backend (MongoDB via Cloud9)
        WineApi.service.createAccount(user).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Toast.makeText(
                        requireContext(),
                        "Account created successfully in MongoDB!",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Navigate to the SignIn page
                    findNavController().navigate(R.id.navigation_signin)
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Create account failed: ${response.errorBody()?.string()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()

            }
        })
    }

//    private fun expandView(view: View) {
//        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED)
//        val targetHeight = view.measuredHeight
//
//        view.layoutParams.height = 0
//        view.visibility = View.VISIBLE
//
//        val animator = ObjectAnimator.ofInt(view, "layoutParams.height", 0, targetHeight)
//        animator.duration = 300
//        animator.interpolator = AccelerateDecelerateInterpolator()
//        animator.addUpdateListener {
//            view.requestLayout()
//        }
//        animator.start()
//    }

//    private fun collapseView(view: View) {
//        val initialHeight = view.measuredHeight
//
//        val animator = ObjectAnimator.ofInt(view, "layoutParams.height", initialHeight, 0)
//        animator.duration = 300
//        animator.interpolator = AccelerateDecelerateInterpolator()
//        animator.addUpdateListener {
//            view.requestLayout()
//        }
//        animator.start()
//
//        animator.doOnEnd {
//            view.visibility = View.GONE
//        }
//    }


}

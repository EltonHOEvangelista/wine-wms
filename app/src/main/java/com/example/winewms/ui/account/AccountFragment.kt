package com.example.winewms.ui.account

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.databinding.FragmentAccountBinding
import androidx.lifecycle.Observer
import com.example.winewms.data.sql.DatabaseHelper

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    //variable used to transfer objects among activities and fragments
    private val accountViewModel: AccountViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAccountBinding.inflate(inflater)

        setupClickListeners()

        // Load featured wines
        loadAccountForm()

        return binding.root
    }

    private fun loadAccountForm() {
        accountViewModel.account.observe(viewLifecycleOwner, Observer { account ->
            if (account != null) {
                binding.txtFirstName.setText(account.firstName)
            }
            if (account != null) {
                binding.txtLastName.setText(account.lastName)
            }
            if (account != null) {
                binding.txtEmail.setText(account.email)
            }
            if (account != null) {
                binding.txtPassword.setText(account.password)
            }
            if (account != null) {
                binding.txtPhone.setText(account.phone)
            }

            if (account != null) {
                binding.txtAddress.setText(account.address?.address ?: "")
            }
            if (account != null) {
                binding.txtCity.setText(account.address?.city ?: "")
            }
            if (account != null) {
                binding.txtPostalCode.setText(account.address?.postalCode ?: "")
            }
            if (account != null) {
                binding.txtProvince.setText(account.address?.province ?: "")
            }
            if (account != null) {
                binding.txtCountry.setText(account.address?.country ?: "")
            }

            //display signout and hide signin
            binding.linearLayoutLogout.visibility = View.VISIBLE
            binding.linearLayoutSignin.visibility = View.GONE
        })
    }

    private fun setupClickListeners() {

        //set click listener on linear Layout Account Detail to show Account Form
        binding.linearLayoutAccountDetail.setOnClickListener() {
            displayAccountForm()
        }

        // Set click listener for Signin
        binding.imgSignin.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_navigation_signin)
        }

        // Set click listener for Signout
        binding.imgSignout.setOnClickListener {

            val dbHelper by lazy {
                DatabaseHelper(requireContext())
            }

            val success = dbHelper.signout(accountViewModel.account.value?.accountId ?: "0")
            if (success) {
                // Clean data in the ViewModel
                accountViewModel.clearAccount()

                //display signout and hide signin
                binding.linearLayoutLogout.visibility = View.GONE
                binding.linearLayoutSignin.visibility = View.VISIBLE

                Toast.makeText(requireContext(), "Good bye!", Toast.LENGTH_SHORT).show()
                // Navigate to Homa Fragment
                findNavController().navigate(R.id.navigation_home)
                //findNavController().navigate(R.id.action_navigation_signup_to_navigation_account)
            } else {
                Toast.makeText(requireContext(), "Fail to signout. Please try again.", Toast.LENGTH_SHORT).show()
            }
            dbHelper.close()
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
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
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.sql.DatabaseHelper
import com.example.winewms.ui.account.signin.SigninModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AccountFragment : Fragment() {

    private lateinit var binding: FragmentAccountBinding

    //variable used to transfer objects among activities and fragments
    private val accountViewModel: AccountViewModel by activityViewModels()

    //Instantiate Wine Api
    var wineApi = WineApi.retrofit.create(WineApiService::class.java)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentAccountBinding.inflate(inflater)

        setupClickListeners()

        // Load account details from Account View Model
        loadAccountDetails()

        return binding.root
    }

    //Function to load account details
    //Display account data only if logged in
    private fun loadAccountDetails() {
        accountViewModel.account.observe(viewLifecycleOwner, Observer { account ->
            if (account != null) {
                binding.txtFirstName.setText(account.firstName)
                binding.txtLastName.setText(account.lastName)
                binding.txtEmail.setText(account.email)
                binding.txtPassword.setText(account.password)
                binding.txtType.setText(if (account.type == 0) "Customer" else "Administrator")
                binding.txtPhone.setText(account.phone)
                binding.txtAddress.setText(account.address?.address ?: "")
                binding.txtCity.setText(account.address?.city ?: "")
                binding.txtPostalCode.setText(account.address?.postalCode ?: "")
                binding.txtProvince.setText(account.address?.province ?: "")
                binding.txtCountry.setText(account.address?.country ?: "")

                //display signout and hide signin
                binding.linearLayoutLogout.visibility = View.VISIBLE
                binding.linearLayoutSignin.visibility = View.GONE

                //set click listener on linear Layout Account Detail
                binding.linearLayoutAccountDetail.setOnClickListener() {
                    if (binding.linearLayoutAccountForm.visibility == View.GONE) {
                        binding.linearLayoutAccountForm.visibility = View.VISIBLE
                        binding.imgArrowAccountDetails.visibility = View.GONE
                    }
                    else {
                        binding.linearLayoutAccountForm.visibility = View.GONE
                        binding.imgArrowAccountDetails.visibility = View.VISIBLE
                    }
                }

                //set click listener on linear Layout Signoff
                binding.linearLayoutSignoffAccount.setOnClickListener() {
                    if (binding.linearLayoutSignoffDetails.visibility == View.GONE) {
                        binding.linearLayoutSignoffDetails.visibility = View.VISIBLE
                    }
                    else {
                        binding.linearLayoutSignoffDetails.visibility = View.GONE
                    }
                }
            }
        })
    }

    private fun setupClickListeners() {
        // Set click listener for Signin
        binding.linearLayoutSignin.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_account_to_navigation_signin)
        }

        // Set click listener for Signout
        binding.linearLayoutLogout.setOnClickListener {
            signoutAccount()
        }

        //set click listener to Signoff button
        binding.btnSignoffAccount.setOnClickListener() {
            signoffAccount()
        }
    }

    //function to singout account
    private fun signoutAccount() {
        val dbHelper by lazy {
            DatabaseHelper(requireContext())
        }

        val success = dbHelper.signout(accountViewModel.account.value?.accountId ?: "0")
        if (success) {

            //Deactivate UI admin features. Type = 1 (administrator)
            if (accountViewModel.account.value?.type == 1) {
                val navView = requireActivity().findViewById<BottomNavigationView>(R.id.nav_view)
                navView.menu.findItem(R.id.navigation_control)?.isVisible = false
                navView.menu.findItem(R.id.navigation_control)?.isEnabled = false
            }

            // Clean data in the ViewModel
            accountViewModel.clearAccount()

            Toast.makeText(requireContext(), "Good bye!", Toast.LENGTH_SHORT).show()



            // Navigate to Homa Fragment
            findNavController().navigate(R.id.navigation_home)
            //findNavController().navigate(R.id.action_navigation_signup_to_navigation_account)
        } else {
            Toast.makeText(requireContext(), "Fail to signout. Please try again.", Toast.LENGTH_SHORT).show()
        }
        dbHelper.close()
    }

    //function to singoff account (cancel account)
    private fun signoffAccount() {
        val email = binding.txtSignoffEmail.text.toString().trim()
        val password = binding.txtSignoffPassword.text.toString().trim()

        if (email.isNotEmpty() && password.isNotEmpty()) {

            //Fetch data from api
            val apiCall = wineApi.deleteAccount(email, password)

            //Asynchronous call to signoff account
            apiCall.enqueue(object : Callback<AccountDataWrapper> {
                override fun onFailure(call: Call<AccountDataWrapper>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
                override fun onResponse(call: Call<AccountDataWrapper>, response: Response<AccountDataWrapper>) {
                    if (response.isSuccessful) {
                        val dataWrapper = response.body()
                        if (dataWrapper != null) {
                            if (dataWrapper.requestStatus) {
                                //Signout locally
                                signoutAccount()
                            } else {
                                Toast.makeText(requireContext(), "Signoff fail: ${dataWrapper.message}", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Signoff fail: ${response.errorBody()?.string()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
        } else {
            Toast.makeText(requireContext(), "Please, enter your email and password", Toast.LENGTH_SHORT).show()
        }
    }
}
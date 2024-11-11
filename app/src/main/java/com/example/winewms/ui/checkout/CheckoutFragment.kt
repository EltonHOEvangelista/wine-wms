package com.example.winewms.ui.checkout

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.data.model.CartWineViewModel
import com.example.winewms.databinding.FragmentCheckoutBinding
import com.example.winewms.ui.checkout.adapter.CheckoutWinesAdapter

class CheckoutFragment : Fragment() {

    private lateinit var binding: FragmentCheckoutBinding
    private val cartWineViewModel: CartWineViewModel by activityViewModels()
    private lateinit var checkoutAdapter: CheckoutWinesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckoutBinding.inflate(inflater)

        setupRecyclerView()
        observeCartItems()
        setupAddressCheckbox()

        binding.btnPlaceOrder.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_checkout_to_navigation_cart)
        }

        binding.btnCancel.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_checkout_to_navigation_cart)
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        checkoutAdapter = CheckoutWinesAdapter(emptyList())
        binding.recyclerViewOrderSummary.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = checkoutAdapter
        }
    }

    private fun observeCartItems() {
        cartWineViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            checkoutAdapter.updateCartItems(cartItems)

            // Calculate total price if needed
            val totalPrice = cartItems.sumOf {
                it.wine.price.toDouble() * (1 - it.wine.discount.toDouble()) * it.quantity
            }
            binding.txtTotalPrice.text = String.format("Total: $%.2f", totalPrice)
        })
    }

    private fun setupAddressCheckbox() {
        // Set initial state of address fields based on checkbox
        toggleAddressFields(!binding.chkUseUserAddress.isChecked)

        // Add a listener to toggle EditText fields based on checkbox state
        binding.chkUseUserAddress.setOnCheckedChangeListener { _, isChecked ->
            toggleAddressFields(!isChecked)
        }
    }

    private fun toggleAddressFields(isEnabled: Boolean) {
        binding.edtAddressLine1.isEnabled = isEnabled
        binding.edtCity.isEnabled = isEnabled
        binding.edtProvince.isEnabled = isEnabled
        binding.edtPostalCode.isEnabled = isEnabled
    }
}

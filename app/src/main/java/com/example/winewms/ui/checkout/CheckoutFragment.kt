package com.example.winewms.ui.checkout

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.R
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.model.*
import com.example.winewms.data.model.payloads.PurchaseItem
import com.example.winewms.data.model.payloads.PurchaseRequest
import com.example.winewms.data.model.payloads.PurchaseResponse
import com.example.winewms.databinding.FragmentCheckoutBinding
import com.example.winewms.ui.checkout.adapter.CheckoutWinesAdapter
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckoutFragment : Fragment() {

    private lateinit var binding: FragmentCheckoutBinding
    private val cartWineViewModel: CartWineViewModel by activityViewModels()
    private lateinit var checkoutAdapter: CheckoutWinesAdapter
    private val wineApiService: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckoutBinding.inflate(inflater)

        setupRecyclerView()
        observeCartItems()
        setupAddressCheckbox()

        binding.btnPlaceOrder.setOnClickListener {
            placeOrder()
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
            updateTotalPrice(cartItems)
        })
    }

    private fun updateTotalPrice(cartItems: List<CartItemModel>) {
        val totalPrice = cartItems.sumOf {
            it.wine.price.toDouble() * (1 - it.wine.discount.toDouble()) * it.quantity
        }
        binding.txtTotalPrice.text = String.format("Total: $%.2f", totalPrice)
    }

    private fun setupAddressCheckbox() {
        toggleAddressFields(!binding.chkUseUserAddress.isChecked)
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

    private fun placeOrder() {
        // Prepare items and address information
        val items = cartWineViewModel.cartItems.value?.map {
            PurchaseItem(
                wineId = it.wine.id,
                quantity = it.quantity
            )
        } ?: emptyList()

        val purchaseRequest = PurchaseRequest(
            userId = "605c72b1e708d84b7d6a7b3e", // Replace with actual user ID
            items = items,
            address = if (binding.chkUseUserAddress.isChecked) "123 Main Street" else binding.edtAddressLine1.text.toString(),
            city = if (binding.chkUseUserAddress.isChecked) "Sample City" else binding.edtCity.text.toString(),
            province = if (binding.chkUseUserAddress.isChecked) "Sample Province" else binding.edtProvince.text.toString(),
            postalCode = if (binding.chkUseUserAddress.isChecked) "12345" else binding.edtPostalCode.text.toString()
        )

        // Call the backend API to place the order
        wineApiService.placeOrder(purchaseRequest).enqueue(object : Callback<PurchaseResponse> {
            override fun onResponse(call: Call<PurchaseResponse>, response: Response<PurchaseResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Order placed successfully!", Toast.LENGTH_LONG).show()

                    // Clear the cart in the ViewModel
                    cartWineViewModel.updateCartItems(emptyList())

                    // Navigate back to the cart screen
                    findNavController().navigate(R.id.action_navigation_checkout_to_navigation_cart)
                } else {
                    // Parsing the error response for insufficient stock
                    val errorBody = response.errorBody()?.string()
                    if (errorBody != null && errorBody.contains("Insufficient stock")) {
                        handleInsufficientStockError(errorBody)
                    } else {
                        Toast.makeText(context, "Order failed: ${response.code()} ${errorBody}", Toast.LENGTH_LONG).show()
                        Log.e("CheckoutFragment", "Order failed with response code: ${response.code()} and message: ${errorBody}")
                    }
                }
            }

            override fun onFailure(call: Call<PurchaseResponse>, t: Throwable) {
                Log.e("CheckoutFragment", "Order failed due to network or parsing error: ${t.message}", t)
                Toast.makeText(context, "Order failed: ${t.message}", Toast.LENGTH_LONG).show()
            }
        })
    }

    private fun handleInsufficientStockError(errorBody: String) {
        try {
            val json = JsonParser.parseString(errorBody).asJsonObject
            val insufficientItems = json["insufficient_stock_items"].asJsonArray

            val updatedCartItems = cartWineViewModel.cartItems.value?.map { cartItem ->
                insufficientItems.firstOrNull {
                    it.asJsonObject["wine_id"].asString == cartItem.wine.id
                }?.let { item ->
                    val availableStock = item.asJsonObject["available_stock"].asInt
                    cartItem.wine.stock = availableStock // Update stock in cart item

                    // Set stock notification message
                    cartItem.stockNotification = "Only $availableStock items left"

                    cartItem
                } ?: cartItem
            } ?: emptyList()

            // Update cart items in ViewModel and navigate back
            cartWineViewModel.updateCartItems(updatedCartItems)

            Toast.makeText(context, "One or more items are out of stock. Please review your cart.", Toast.LENGTH_LONG).show()
            findNavController().navigate(R.id.action_navigation_checkout_to_navigation_cart)
        } catch (e: Exception) {
            Log.e("CheckoutFragment", "Failed to parse insufficient stock error response", e)
        }
    }

}

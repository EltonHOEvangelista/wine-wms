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
import com.example.winewms.databinding.FragmentCheckoutBinding
import com.example.winewms.ui.account.AccountAddressModel
import com.example.winewms.ui.account.AccountViewModel
import com.example.winewms.ui.checkout.adapter.CheckoutWinesAdapter
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CheckoutFragment : Fragment() {

    private lateinit var binding: FragmentCheckoutBinding
    private val cartWineViewModel: CartWineViewModel by activityViewModels()
    private val accountViewModel: AccountViewModel by activityViewModels()
    private val salesResponseViewModel: SalesResponseViewModel by activityViewModels()
    private lateinit var checkoutAdapter: CheckoutWinesAdapter
    private var signedInAccount: Boolean = false
    private val wineApiService: WineApiService by lazy { WineApi.retrofit.create(WineApiService::class.java) }
    private var invoiceList = mutableListOf<WineInvoice>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCheckoutBinding.inflate(inflater)

        //Setup recycler view
        setupRecyclerView()

        //Load cart items
        observeCartItems()

        //Load account details
        loadAccountDetails()

        //Setup click listeners
        setClickListeners()

        //Setup shipping address
        setupAddressCheckbox()

        return binding.root
    }

    private fun setClickListeners() {
        binding.btnPlaceOrder.setOnClickListener {
            placeOrder()
        }
        binding.btnCancel.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_checkout_to_navigation_cart)
        }
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
            it.wine.sale_price.toDouble() * (1 - it.wine.discount.toDouble()) * it.quantity
        }
        binding.txtTotalPrice.text = String.format("Total: $%.2f +tax", totalPrice)
    }

    private fun setupAddressCheckbox() {
        binding.chkUseUserAddress.isChecked
        binding.chkUseUserAddress.setOnCheckedChangeListener { _, isChecked ->
            binding.edtAddressLine1.isEnabled = !isChecked
            binding.edtCity.isEnabled = !isChecked
            binding.edtProvince.isEnabled = !isChecked
            binding.edtPostalCode.isEnabled = !isChecked
            binding.edtCountry.isEnabled = !isChecked
        }
    }

    //Function to load account details
    private fun loadAccountDetails() {
        accountViewModel.account.observe(viewLifecycleOwner, Observer { account ->
            if (account != null) {
                signedInAccount = true
                binding.edtAddressLine1.setText(account.address?.address ?: "")
                binding.edtCity.setText(account.address?.city ?: "")
                binding.edtPostalCode.setText(account.address?.postalCode ?: "")
                binding.edtProvince.setText(account.address?.province ?: "")
                binding.edtCountry.setText(account.address?.country ?: "")


            }
        })
    }

    //function to validate shipping address
    private fun shippingAddress(): AccountAddressModel? {

        val address = binding.edtAddressLine1.text.toString()
        val city = binding.edtCity.text.toString()
        val province = binding.edtProvince.text.toString()
        val postalCode = binding.edtPostalCode.text.toString()
        val country = binding.edtCountry.text.toString()


        val shippingAddress = if (address.isNotEmpty() || city.isNotEmpty() || province.isNotEmpty() || postalCode.isNotEmpty()) {
            AccountAddressModel(
                address = address,
                city = city,
                province = province,
                postalCode = postalCode,
                country = country
            )
        } else { null }

        return  shippingAddress
    }

    //Function to place order
    private fun placeOrder() {

        //Verify customer account status (signed in)
        if (signedInAccount) {
            val address = shippingAddress()
            if (address != null) {

                //load customer account
                val salesAccount = accountViewModel.account.value?.let {
                    SalesAccount(
                        accountId = it.accountId,
                        email = it.email,
                        phone = it.phone
                    )
                }

                // Prepare items and address information
                val items = cartWineViewModel.cartItems.value?.map {
                    SalesItem(
                        wineId = it.wine.id,
                        quantity = it.quantity
                    )
                } ?: emptyList()

                val salesRequest = salesAccount?.let {
                    SalesModel(
                        invoiceId = "0",
                        items =  items,
                        account = it,
                        shippingAddress = address
                    )
                }

                // Call the backend API to place the order
                if (salesRequest != null) {
                    wineApiService.placeSalesOrder(salesRequest).enqueue(object : Callback<SalesDataWrapper> {
                        override fun onResponse(call: Call<SalesDataWrapper>, response: Response<SalesDataWrapper>) {
                            if (response.isSuccessful) {
                                // Clear the cart in the ViewModel
                                cartWineViewModel.updateCartItems(emptyList())

                                val salesDataWrapper = response.body()
                                salesDataWrapper?.let {
                                    when {
                                        it.invoices.isNotEmpty() && it.saleRefused.isEmpty() -> {
                                            Toast.makeText(
                                                context,
                                                "Thank you for shopping at Wine Warehouse. Your order has been placed successfully!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                        it.invoices.isNotEmpty() && it.saleRefused.isNotEmpty() -> {
                                            Toast.makeText(
                                                context,
                                                "Thank you for shopping at Wine Warehouse. Your order has been partially placed",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            // List wines out of stock
                                        }
                                    }
                                    it.invoices?.let { invoices ->
                                        salesResponseViewModel.setWineInvoiceList(invoices)
                                        displayInvoice()
                                    }
                                }

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

                        override fun onFailure(call: Call<SalesDataWrapper>, t: Throwable) {
                            Log.e("CheckoutFragment", "Order failed due to network or parsing error: ${t.message}", t)
                            Toast.makeText(context, "Order failed: ${t.message}", Toast.LENGTH_LONG).show()
                        }
                    })
                }

            }
            else {
                Toast.makeText(context, "Please, inform shipping address.", Toast.LENGTH_LONG).show()
            }
        }
        else {
            Toast.makeText(context, "Please, sign in Wine Warehouse to place orders.", Toast.LENGTH_LONG).show()
        }
    }

    private fun displayInvoice() {

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


//                    cartItem.wine.stock = availableStock // Update stock in cart item

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
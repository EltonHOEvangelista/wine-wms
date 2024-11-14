package com.example.winewms.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.navigation.fragment.findNavController
import com.example.winewms.R
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.data.model.CartWineViewModel
import com.example.winewms.databinding.FragmentCartBinding
import com.example.winewms.ui.cart.adapter.cart.CartWinesAdapter
import com.example.winewms.ui.cart.adapter.cart.OnCartWinesClickListener
import com.google.android.material.bottomnavigation.BottomNavigationView

class CartFragment : Fragment(), OnCartWinesClickListener {

    private lateinit var binding: FragmentCartBinding
    private val cartWineViewModel: CartWineViewModel by activityViewModels()
    private lateinit var cartAdapter: CartWinesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater)

        setupRecyclerView()

        binding.btnCheckout.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_cart_to_navigation_checkout)
        }

        binding.btnResetCart.setOnClickListener {
            resetCart()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        observeCartItems() // Re-observe to update when navigating back
    }

    private fun setupRecyclerView() {
        cartAdapter = CartWinesAdapter(emptyList(), this)
        binding.recyclerViewCartItems.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = cartAdapter
        }
    }

    private fun observeCartItems() {
        cartWineViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            cartAdapter.updateCartItems(cartItems)

            // Enable or disable the checkout button based on cart contents
            binding.btnCheckout.isEnabled = cartItems.isNotEmpty()
            binding.btnCheckout.alpha = if (cartItems.isNotEmpty()) 1f else 0.5f // Full opacity if enabled, 50% opacity if disabled

            // Update UI visibility for empty cart
            if (cartItems.isEmpty()) {
                binding.txtEmptyCart.visibility = View.VISIBLE
                binding.recyclerViewCartItems.visibility = View.GONE
            } else {
                binding.txtEmptyCart.visibility = View.GONE
                binding.recyclerViewCartItems.visibility = View.VISIBLE
            }

            updateTotalPrice(cartItems)

            // Update cart badge
            updateCardBadge()
        })
    }

    private fun updateCardBadge() {

        // Find your BottomNavigationView
        val navView: BottomNavigationView? = activity?.findViewById(R.id.nav_view)
        //val navView: BottomNavigationView = binding.root.findViewById(R.id.nav_view)

        val menuItemId = R.id.navigation_cart
        val badgeDrawable = navView!!.getOrCreateBadge(menuItemId)

        if (cartWineViewModel.cartItems.value?.size!! > 0) {
            // Set the badge number
            badgeDrawable.isVisible = true  // Show the badge
            badgeDrawable.number = cartWineViewModel.cartItems.value?.size ?: 0       // Set the number to show on the badge

            // Optionally customize the badge's appearance
            badgeDrawable.badgeTextColor = ContextCompat.getColor(requireContext(), android.R.color.white)
            badgeDrawable.backgroundColor = ContextCompat.getColor(requireContext(), R.color.DarkRedWine)
        }
        else {
            badgeDrawable.isVisible = false  // remove badge
        }
    }

    private fun updateTotalPrice(cartItems: List<CartItemModel>) {
        val totalPrice = cartItems.sumOf {
            it.wine.price.toDouble() * (1 - it.wine.discount.toDouble()) * it.quantity
        }
        binding.txtTotalPrice.text = String.format("Total: $%.2f", totalPrice)
    }

    private fun resetCart() {
        cartWineViewModel.updateCartItems(emptyList())
        Toast.makeText(context, "Cart has been reset", Toast.LENGTH_SHORT).show()
    }

    override fun onCartWinesClickListener(model: CartItemModel) {
        Toast.makeText(context, "Selected ${model.wine.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onIncreaseQuantityClick(model: CartItemModel) {
        if (model.quantity < model.wine.stock) {
            model.quantity += 1
            updateCartInViewModel()
            Toast.makeText(context, "Increased quantity of ${model.wine.name}", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Only ${model.wine.stock} items available in stock", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDecreaseQuantityClick(model: CartItemModel) {
        if (model.quantity > 1) {
            model.quantity -= 1
            updateCartInViewModel()
            Toast.makeText(context, "Decreased quantity of ${model.wine.name}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRemoveItemClick(model: CartItemModel) {
        val currentList = cartWineViewModel.cartItems.value?.toMutableList()
        currentList?.remove(model)
        cartWineViewModel.updateCartItems(currentList ?: emptyList())
        Toast.makeText(context, "Removed ${model.wine.name} from cart", Toast.LENGTH_SHORT).show()
    }

    private fun updateCartInViewModel() {
        val updatedList = cartWineViewModel.cartItems.value ?: emptyList()
        cartWineViewModel.updateCartItems(updatedList)
    }
}

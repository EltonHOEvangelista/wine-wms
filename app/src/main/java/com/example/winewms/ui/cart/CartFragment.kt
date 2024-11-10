package com.example.winewms.ui.cart

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.data.model.CartWineViewModel
import com.example.winewms.databinding.FragmentCartBinding
import com.example.winewms.ui.cart.adapter.cart.CartWinesAdapter
import com.example.winewms.ui.cart.adapter.cart.OnCartWinesClickListener

class CartFragment : Fragment(), OnCartWinesClickListener {

    private lateinit var binding: FragmentCartBinding
    private val cartWineViewModel: CartWineViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCartBinding.inflate(inflater)

        setupRecyclerView()
        observeCartItems()

        binding.btnCheckout.setOnClickListener {
            Toast.makeText(context, "Proceeding to checkout!", Toast.LENGTH_SHORT).show()
            // Add logic for checkout process
        }

        return binding.root
    }

    private fun setupRecyclerView() {
        binding.recyclerViewCartItems.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            adapter = CartWinesAdapter(emptyList(), this@CartFragment)
        }
    }

    private fun observeCartItems() {
        cartWineViewModel.cartItems.observe(viewLifecycleOwner, Observer { cartItems ->
            val adapter = CartWinesAdapter(cartItems, this)
            binding.recyclerViewCartItems.adapter = adapter

            // Calculate total price
            val totalPrice = cartItems.sumOf { it.wine.price.toDouble() * it.quantity }
            binding.txtTotalPrice.text = String.format("Total: $%.2f", totalPrice)
        })
    }

    private fun updateCartInViewModel() {
        val updatedList = cartWineViewModel.cartItems.value ?: emptyList()
        cartWineViewModel.updateCartItems(updatedList)
    }

    override fun onCartWinesClickListener(model: CartItemModel) {
        Toast.makeText(context, "Selected ${model.wine.name}", Toast.LENGTH_SHORT).show()
    }

    override fun onIncreaseQuantityClick(model: CartItemModel) {
        model.quantity += 1
        updateCartInViewModel()
        Toast.makeText(context, "Increased quantity of ${model.wine.name}", Toast.LENGTH_SHORT).show()
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
}

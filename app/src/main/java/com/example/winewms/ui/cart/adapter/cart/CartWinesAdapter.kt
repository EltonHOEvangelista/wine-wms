package com.example.winewms.ui.cart.adapter.cart

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.databinding.CartWineCardBinding
import com.example.winewms.databinding.WineCardBinding
import com.squareup.picasso.Picasso

class CartWinesAdapter(
    private var cartItems: List<CartItemModel>,
    private val listener: OnCartWinesClickListener,
) : RecyclerView.Adapter<CartWinesAdapter.CartWineViewHolder>() {

    inner class CartWineViewHolder(private val binding: CartWineCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItemModel) {
            val wine = cartItem.wine
            val wineCardBinding = WineCardBinding.bind(binding.wineCard.root)
            wineCardBinding.apply {
                txtPrice.text = String.format("$%.2f", wine.price)
                txtWineName.text = wine.name
                txtWineProcuder.text = wine.producer
                txtWineCountry.text = wine.country
                Picasso.get().load(wine.image_path).into(imgBottle)
            }

            val discount = wine.discount
            val discountedPrice = wine.price * (1 - discount)

            binding.apply {
                // Set stock notification if available
                if (!cartItem.stockNotification.isNullOrEmpty()) {
                    txtStockNotification.text = cartItem.stockNotification
                    txtStockNotification.visibility = View.VISIBLE
                } else {
                    txtStockNotification.visibility = View.GONE
                }

                txtDiscount.text = "Discount: ${(discount * 100).toInt()}%"
                txtDiscountedPrice.text = String.format("Final Price: $%.2f", discountedPrice)
                txtQuantity.text = cartItem.quantity.toString()

                btnIncreaseQuantity.setOnClickListener {
                    listener.onIncreaseQuantityClick(cartItem)
                }
                btnDecreaseQuantity.setOnClickListener {
                    listener.onDecreaseQuantityClick(cartItem)
                }
                btnRemoveItem.setOnClickListener {
                    listener.onRemoveItemClick(cartItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartWineViewHolder {
        val binding = CartWineCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CartWineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartWineViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    // Method to update the cart items selectively
    fun updateCartItems(newItems: List<CartItemModel>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}

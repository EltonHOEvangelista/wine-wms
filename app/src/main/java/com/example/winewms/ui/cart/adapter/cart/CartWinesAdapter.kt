package com.example.winewms.ui.cart.adapter.cart

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.databinding.CartWineCardBinding
import com.example.winewms.databinding.WineCardBinding
import com.squareup.picasso.Picasso

class CartWinesAdapter(
    private val cartItems: List<CartItemModel>,
    private val listener: OnCartWinesClickListener,
) : RecyclerView.Adapter<CartWinesAdapter.CartWineViewHolder>() {

    // ViewHolder class for cart wine items
    inner class CartWineViewHolder(private val binding: CartWineCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItemModel) {
            val wine = cartItem.wine

            // Bind wine information within wine_card.xml
            val wineCardBinding = WineCardBinding.bind(binding.wineCard.root)
            wineCardBinding.apply {
                txtPrice.text = String.format("$%.2f", wine.price)
                txtWineName.text = wine.name
                txtWineProcuder.text = wine.producer
                txtWineCountry.text = wine.country
                Picasso.get().load(wine.image_path).into(imgBottle)
            }

            // Calculate discounted price and set discount information
            val discount = wine.discount
            val discountedPrice = wine.price * (1 - discount)

            binding.apply {
                txtDiscount.text = "Discount: ${(discount*100).toInt()}%"
                txtDiscountedPrice.text = String.format("Final Price: $%.2f", discountedPrice)
                txtQuantity.text = cartItem.quantity.toString()

                // Set click listeners for quantity adjustment and item removal
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
}

package com.example.winewms.ui.checkout.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R
import com.example.winewms.data.model.CartItemModel
import com.example.winewms.databinding.CheckoutWineCardBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class CheckoutWinesAdapter(
    private var cartItems: List<CartItemModel>
) : RecyclerView.Adapter<CheckoutWinesAdapter.CheckoutWineViewHolder>() {

    inner class CheckoutWineViewHolder(private val binding: CheckoutWineCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItemModel) {
            val wine = cartItem.wine
            val discount = wine.discount
            val discountedPricePerUnit = wine.sale_price * (1 - discount)
            val totalDiscountedPrice = discountedPricePerUnit * cartItem.quantity

            binding.apply {
                txtWineName.text = wine.name
                txtQuantity.text = "Quantity: ${cartItem.quantity}"
                txtOriginalTotalPrice.text = String.format("Original: $%.2f", wine.sale_price * cartItem.quantity)
                txtDiscount.text = String.format("Discount: %d%%", (discount * 100).toInt())
                txtDiscountedTotal.text = String.format("Discounted Total: $%.2f", totalDiscountedPrice)

                //Load image from Google Firebase
                val storageRef = Firebase.storage.reference.child(wine.image_path)
                // Fetch the image URL from Firebase Storage
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri.toString())
                        .error(R.drawable.wine_bottle_t)
                        .into(imgBottle)
                }.addOnFailureListener {
                    imgBottle.setImageResource(R.drawable.wine_bottle_t)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CheckoutWineViewHolder {
        val binding = CheckoutWineCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CheckoutWineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CheckoutWineViewHolder, position: Int) {
        holder.bind(cartItems[position])
    }

    override fun getItemCount() = cartItems.size

    fun updateCartItems(newItems: List<CartItemModel>) {
        cartItems = newItems
        notifyDataSetChanged()
    }
}

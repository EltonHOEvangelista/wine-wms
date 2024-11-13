package com.example.winewms.ui.cart.adapter.cart

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R
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

            // Calculate discounted price and set discount information
            val discount = wine.discount
            val discountedPrice = wine.price * (1 - discount)

            // Access views within wine_card.xml using wineCardBinding
            wineCardBinding.apply {
                txtPrice.text = String.format("$%.2f", discountedPrice)
                txtWineName.text = wine.name
                txtWineProcuder.text = wine.producer
                txtWineCountry.text = wine.country
                Picasso.get().load(wine.image_path).into(imgBottle)
            }

            // Display discount if exists.
            if (discount > 0.00) {
                binding.txtDiscount.visibility = View.VISIBLE
                binding.txtDiscount.text = "Save: ${(discount * 100).toInt()}%"
                binding.txtOriginalPrice.visibility = View.VISIBLE
                binding.txtOriginalPrice.text = String.format("$%.2f", wine.price)
                binding.txtOriginalPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }

            binding.apply {
                // Set stock notification if available
                if (!cartItem.stockNotification.isNullOrEmpty()) {
                    txtStockNotification.text = cartItem.stockNotification
                    txtStockNotification.visibility = View.VISIBLE
                } else {
                    txtStockNotification.visibility = View.GONE
                }

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

            // Set Wine Description
            binding.txtWineDescription.text = wine.description

            // Set Wine Type
            binding.txtType.text = wine.type
            when (wine.type.lowercase()) {
                "red" -> binding.imgType.setImageResource(R.drawable.glass_red_wine_24)
                "white" -> binding.imgType.setImageResource(R.drawable.glass_white_wine_24)
                "sparkling" -> binding.imgType.setImageResource(R.drawable.glass_white_wine_24)
                "rose" -> binding.imgType.setImageResource(R.drawable.glass_rose_wine_24)
                else -> binding.imgType.setImageResource((R.drawable.glass_red_wine_24))
            }

            // Set Wine Harvest
            binding.txtHarvest.text = wine.harvest.toString()

            // Set Wine Grapes
            binding.txtGrapes.text = wine.grapes.joinToString(", ")

            // Function to set LayoutParams based on taste characteristic
            fun setTasteCharacteristic(layout: LinearLayout, weight: Float) {
                val params = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.MATCH_PARENT
                ).apply {
                    this.weight = weight / 10f
                }
                layout.layoutParams = params
            }

            // Set Taste Characteristics
            with(wine.taste_characteristics) {
                setTasteCharacteristic(binding.linearLayoutLLightnessRate, lightness.toFloat())
                setTasteCharacteristic(binding.linearLayoutLLightnessDiff, 10.0f - lightness.toFloat())
                setTasteCharacteristic(binding.linearLayoutLTanninRate, tannin.toFloat())
                setTasteCharacteristic(binding.linearLayoutLTanninDiff, 10.0f - lightness.toFloat())
                setTasteCharacteristic(binding.linearLayoutLDrynessRate, dryness.toFloat())
                setTasteCharacteristic(binding.linearLayoutLDrynessDiff, 10.0f - lightness.toFloat())
                setTasteCharacteristic(binding.linearLayoutLAcidityRate, acidity.toFloat())
                setTasteCharacteristic(binding.linearLayoutLAcidityDiff, 10.0f - lightness.toFloat())
            }

            // Set Wine Food Pair
            binding.txtFoodPair.text = wine.food_pair.joinToString(", ")

            // Set Wine's Review
            fun createReviewTextView(context: Context, reviewText: String): TextView {
                return TextView(context).apply {
                    text = "\" $reviewText \""
                    textSize = 14f
                    setTypeface(typeface, android.graphics.Typeface.ITALIC)
                    setPadding(8, 8, 8, 8)
                }
            }

            // Add reviews
            wine.reviews.forEach { review ->
                val textView = createReviewTextView(itemView.context, review)
                binding.linearLayoutTextReviews.addView(textView)
            }

            //Expand Wine Details
            binding.linearLayoutWineCard.setOnClickListener {
                if (binding.linearLayoutWineDetails.visibility == View.GONE) {
                    binding.linearLayoutWineDetails.visibility = View.VISIBLE
                }
                else {
                    binding.linearLayoutWineDetails.visibility = View.GONE
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

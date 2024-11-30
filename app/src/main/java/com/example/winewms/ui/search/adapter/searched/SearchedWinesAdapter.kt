package com.example.winewms.ui.search.adapter.searched

import android.content.Context
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.SearchedWineCardBinding
import com.example.winewms.databinding.WineCardBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class SearchedWinesAdapter(
    private val wineList: List<WineModel>,
    private val listener: OnSearchedWinesClickListener,
    private val isAdmin: Boolean
) : RecyclerView.Adapter<SearchedWinesAdapter.SearchedWineViewHolder>() {

    // ViewHolder class for searched wine items
    inner class SearchedWineViewHolder(private val binding: SearchedWineCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(wine: WineModel) {
            // Set visibility based on isAdmin
            binding.btnAddToCart.visibility = if (isAdmin) View.GONE else View.VISIBLE
            binding.btnEditWine.visibility = if (isAdmin) View.VISIBLE else View.GONE
            binding.btnDeleteWine.visibility = if (isAdmin) View.VISIBLE else View.GONE


            // Create a local binding variable for wine_card.xml
            val wineCardBinding = WineCardBinding.bind(binding.wineCard.root)

            // Calculate discounted price and set discount information
            val discount = wine.discount
            val discountedPrice = wine.sale_price * (1 - discount)

            // Access views within wine_card.xml using wineCardBinding
            wineCardBinding.apply {
                txtPrice.text = String.format("$ %.2f", discountedPrice)
                txtWineName.text = wine.name
                txtWineProcuder.text = wine.producer
                txtWineCountry.text = wine.country

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

            // Set discount and discounted price in searched_wine_card.xml
            if (discount > 0.00) {
                binding.txtDiscount.visibility = View.VISIBLE
                binding.txtDiscount.text = "Save: ${(discount * 100).toInt()}%"
                binding.txtOriginalPrice.visibility = View.VISIBLE
                binding.txtOriginalPrice.text = String.format("$%.2f", wine.sale_price)
                binding.txtOriginalPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }

            // Set click listeners for the buttons in searched_wine_card.xml
            binding.btnAddToCart.setOnClickListener {
                if (!isAdmin) { // Ensure this button is usable only by non-admin users
                    listener.onBuyClick(wine)
                    Toast.makeText(binding.root.context, "Buying ${wine.name}", Toast.LENGTH_SHORT).show()
                }
            }

            binding.btnEditWine.setOnClickListener {
                if (isAdmin) { // Only admins can use this button
                    Toast.makeText(binding.root.context, "Editing ${wine.name}", Toast.LENGTH_SHORT).show()
                    // Add navigation to edit fragment or dialog here
                }
            }

            binding.btnEditWine.setOnClickListener {
                if (isAdmin) { // Only admins can use this button
                    Toast.makeText(binding.root.context, "Deleting ${wine.name}", Toast.LENGTH_SHORT).show()
                    // Add navigation to edit fragment or dialog here
                }
            }

            // Set Ranting Bar
            binding.ratingBarWine.rating = wine.rate

            // Set Wine Description
            binding.txtWineDescription.text = wine.description

            // Set Wine Type
            binding.txtType.text = wine.type
            when (wine.type.lowercase()) {
                "red" -> binding.imgType.setImageResource(R.drawable.glass_red_wine_24)
                "white" -> binding.imgType.setImageResource(R.drawable.glass_white_wine_24)
                "sparkling" -> binding.imgType.setImageResource(R.drawable.glass_white_wine_24)
                "rose" -> binding.imgType.setImageResource(R.drawable.glass_rose_wine_24)
                "dessert wine" -> binding.imgType.setImageResource(R.drawable.glass_red_wine_24)
                "orange wine" -> binding.imgType.setImageResource(R.drawable.glass_white_wine_24)
                else -> binding.imgType.setImageResource((R.drawable.glass_red_wine_24))
            }

            // Set Wine Harvest
            binding.txtHarvest.text = wine.harvest_year.toString()

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
                setTasteCharacteristic(binding.linearLayoutLTanninDiff, 10.0f - tannin.toFloat())
                setTasteCharacteristic(binding.linearLayoutLDrynessRate, dryness.toFloat())
                setTasteCharacteristic(binding.linearLayoutLDrynessDiff, 10.0f - dryness.toFloat())
                setTasteCharacteristic(binding.linearLayoutLAcidityRate, acidity.toFloat())
                setTasteCharacteristic(binding.linearLayoutLAcidityDiff, 10.0f - acidity.toFloat())
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchedWineViewHolder {
        val binding = SearchedWineCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SearchedWineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchedWineViewHolder, position: Int) {
        holder.bind(wineList[position])
    }

    override fun getItemCount() = wineList.size
}

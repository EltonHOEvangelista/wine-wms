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
    private val isAdmin: Boolean // Add this parameter to determine user type
) : RecyclerView.Adapter<SearchedWinesAdapter.SearchedWineViewHolder>() {

    // ViewHolder class for searched wine items
    inner class SearchedWineViewHolder(private val binding: SearchedWineCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(wine: WineModel) {
            // Create a local binding variable for wine_card.xml
            val wineCardBinding = WineCardBinding.bind(binding.wineCard.root)

            // Toggle button visibility based on user type
            binding.btnAddToCart.visibility = if (isAdmin) View.GONE else View.VISIBLE
            binding.btnEditWine.visibility = if (isAdmin) View.VISIBLE else View.GONE

            // Set click listener for Add to Cart button
            binding.btnAddToCart.setOnClickListener {
                listener.onBuyClick(wine)
                Toast.makeText(binding.root.context, "Added ${wine.name} to cart", Toast.LENGTH_SHORT).show()
            }

            // Set click listener for Edit button
            binding.btnEditWine.setOnClickListener {
                listener.onEditClick(wine)
                Toast.makeText(binding.root.context, "Editing ${wine.name}", Toast.LENGTH_SHORT).show()
            }

            // Calculate discounted price and set discount information
            val discount = wine.discount
            val discountedPrice = wine.sale_price * (1 - discount)

            // Access views within wine_card.xml using wineCardBinding
            wineCardBinding.apply {
                txtPrice.text = String.format("$ %.2f", discountedPrice)
                txtWineName.text = wine.name
                txtWineProcuder.text = wine.producer
                txtWineCountry.text = wine.country

                // Load image from Firebase
                val storageRef = Firebase.storage.reference.child(wine.image_path)
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri.toString())
                        .error(R.drawable.wine_bottle_t)
                        .into(imgBottle)
                }.addOnFailureListener {
                    imgBottle.setImageResource(R.drawable.wine_bottle_t)
                }
            }

            // Set discount information
            if (discount > 0.00) {
                binding.txtDiscount.visibility = View.VISIBLE
                binding.txtDiscount.text = "Save: ${(discount * 100).toInt()}%"
                binding.txtOriginalPrice.visibility = View.VISIBLE
                binding.txtOriginalPrice.text = String.format("$%.2f", wine.sale_price)
                binding.txtOriginalPrice.paintFlags = Paint.STRIKE_THRU_TEXT_FLAG
            }

            // Set Rating Bar
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
                else -> binding.imgType.setImageResource(R.drawable.glass_red_wine_24)
            }

            // Set Wine Harvest
            binding.txtHarvest.text = wine.harvest_year.toString()

            // Set Wine Grapes
            binding.txtGrapes.text = wine.grapes.joinToString(", ")

            // Expand Wine Details
            binding.linearLayoutWineCard.setOnClickListener {
                if (binding.linearLayoutWineDetails.visibility == View.GONE) {
                    binding.linearLayoutWineDetails.visibility = View.VISIBLE
                } else {
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


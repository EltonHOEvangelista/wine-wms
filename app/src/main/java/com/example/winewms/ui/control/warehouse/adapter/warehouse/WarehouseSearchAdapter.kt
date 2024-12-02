package com.example.winewms.ui.control.warehouse.adapter.warehouse

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R
import com.example.winewms.data.model.WineLocation
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.WarehouseWineCardBinding
import com.example.winewms.ui.account.invoices.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class WarehouseSearchAdapter(
    private val wineList: List<WineModel>,
    private val listener: onWarehouseSearchClickListener,
    var context: Context?,
) : RecyclerView.Adapter<WarehouseSearchAdapter.WarehouseSearchViewHolder>() {

    // ViewHolder class for warehouse searched wine items
    inner class WarehouseSearchViewHolder(private val binding: WarehouseWineCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(wine: WineModel) {

            // Calculate discounted price and set discount information
            val discount = wine.discount
            val discountedPrice = wine.sale_price * (1 - discount)

            binding.warehouseWineCard.txtPrice.text = String.format("$ %.2f", discountedPrice)
            binding.warehouseWineCard.txtWineName.text = wine.name
            binding.warehouseWineCard.txtWineProcuder.text = wine.producer
            binding.warehouseWineCard.txtWineCountry.text = wine.country

            //Load image from Google Firebase
            val storageRef = Firebase.storage.reference.child(wine.image_path)
            // Fetch the image URL from Firebase Storage
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get()
                    .load(uri.toString())
                    .error(R.drawable.wine_bottle_t)
                    .into(binding.warehouseWineCard.imgBottle)
            }.addOnFailureListener {
                binding.warehouseWineCard.imgBottle.setImageResource(R.drawable.wine_bottle_t)
            }

            binding.txtWarehouseStock.text = wine.stock.toString()

            for (wine in wine.stockLocation) {
                val linearLayout = setupStockLocation(wine)
                binding.linearLayoutWarehouseLocation.addView(linearLayout)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseSearchViewHolder {
        val binding = WarehouseWineCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WarehouseSearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WarehouseSearchViewHolder, position: Int) {
        holder.bind(wineList[position])
    }

    override fun getItemCount(): Int = wineList.size

    // Function to setup invoice data
    private fun setupStockLocation(wine: WineLocation): LinearLayout {

        // Create a LinearLayout
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 4.dp, 0, 4.dp)
        }

        // Create and configure the wine aile TextView
        val wineAisle = TextView(context).apply {
            text = "Aile: ${wine.aisle}"
            layoutParams = LinearLayout.LayoutParams(50.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.LEFT
        }

        // Create and configure the wine shelf TextView
        val wineShelf = TextView(context).apply {
            text = "Shelf: ${wine.shelf}"
            layoutParams = LinearLayout.LayoutParams(50.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.LEFT
        }

        // Create and configure the wine shelf TextView
        val wineStock = TextView(context).apply {
            text = "Stock: ${wine.stock}"
            layoutParams = LinearLayout.LayoutParams(50.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.LEFT
        }

        // Add views and dividers to the LinearLayout
        linearLayout.addView(wineAisle)
        linearLayout.addView(wineShelf)
        linearLayout.addView(wineStock)

        return linearLayout
    }
}
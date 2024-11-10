package com.example.winewms.ui.search.adapter.searched

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.SearchedWineCardBinding
import com.example.winewms.databinding.WineCardBinding
import com.squareup.picasso.Picasso

class SearchedWinesAdapter(
    private val wineList: List<WineModel>,
    private val listener: OnSearchedWinesClickListener,
) : RecyclerView.Adapter<SearchedWinesAdapter.SearchedWineViewHolder>() {

    // ViewHolder class for searched wine items
    inner class SearchedWineViewHolder(private val binding: SearchedWineCardBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(wine: WineModel) {
            // Create a local binding variable for wine_card.xml
            val wineCardBinding = WineCardBinding.bind(binding.wineCard.root)

            // Access views within wine_card.xml using wineCardBinding
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

            // Set discount and discounted price in searched_wine_card.xml
            binding.txtDiscount.text = "Discount: ${(discount * 100).toInt()}%"
            binding.txtDiscountedPrice.text = String.format("Final Price: $%.2f", discountedPrice)

            // Set click listeners for the buttons in searched_wine_card.xml
            binding.btnBuy.setOnClickListener {
                listener.onBuyClick(wine)
                Toast.makeText(binding.root.context, "Buying ${wine.name}", Toast.LENGTH_SHORT).show()
            }
            binding.btnDetails.setOnClickListener {
                listener.onDetailsClick(wine)
                Toast.makeText(binding.root.context, "Viewing details for ${wine.name}", Toast.LENGTH_SHORT).show()
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

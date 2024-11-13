package com.example.winewms.ui.home.adapter.featured

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.WineCardBinding
import com.squareup.picasso.Picasso

class FeaturedWinesAdapter (
    private val wineList: List<WineModel>,
    private val listener: onFeaturedWinesClickListener,
) : RecyclerView.Adapter<FeaturedWinesAdapter.WineViewHolder>() {

    //variable to handle selected recyclerView position.
    private var selectedIndex: Int = -1

    // ViewHolder class holds the views for each item in the list.
    inner class WineViewHolder(val binding: WineCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Pass the selected wine to the listener
                    listener.onFeaturedWinesClickListener(wineList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WineViewHolder {
        val binding = WineCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WineViewHolder, position: Int) {

        val item = wineList[position] // Gets the current item from the list
        val unknown = "unknown"
        // Bind data to the views
        holder.binding.txtPrice.text = String.format(" $%.2f", item.price) ?: unknown
        holder.binding.txtWineName.text = item.name ?: unknown
        holder.binding.txtWineProcuder.text = item.producer ?: unknown
        holder.binding.txtWineCountry.text = item.country ?: unknown
        Picasso.get()
            .load(item.image_path)
            .into(holder.binding.imgBottle)
    }

    // Returns the total number of items in the list.
    override fun getItemCount() = wineList.size

    //function to set selected recyclerView position
    fun setSelectedIndex(selectedIndex: Int) {
        this.selectedIndex = selectedIndex
        notifyDataSetChanged()
    }
}
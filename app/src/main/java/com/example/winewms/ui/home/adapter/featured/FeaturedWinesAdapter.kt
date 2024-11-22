package com.example.winewms.ui.home.adapter.featured

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.WineCardBinding
import com.squareup.picasso.Picasso
import com.example.winewms.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

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

        // Gets the current item from the list
        val item = wineList[position]

        //Load image from Google Firebase
        val storageRef = Firebase.storage.reference.child(item.image_path)
        // Fetch the image URL from Firebase Storage
        storageRef.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get()
                .load(uri.toString())
                .error(R.drawable.wine_bottle_t)
                .into(holder.binding.imgBottle)
        }.addOnFailureListener {
            holder.binding.imgBottle.setImageResource(R.drawable.wine_bottle_t)
        }

        //Load following wine data
        val unknown = "unknown"
        holder.binding.txtPrice.text = String.format(" $%.2f", item.sale_price) ?: unknown
        holder.binding.txtWineName.text = item.name ?: unknown
        holder.binding.txtWineProcuder.text = item.producer ?: unknown
        holder.binding.txtWineCountry.text = item.country ?: unknown
    }

    // Returns the total number of items in the list.
    override fun getItemCount() = wineList.size

    //function to set selected recyclerView position
    fun setSelectedIndex(selectedIndex: Int) {
        this.selectedIndex = selectedIndex
        notifyDataSetChanged()
    }
}
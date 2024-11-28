package com.example.winewms.ui.control

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.AdminWineCardBinding
import com.google.firebase.Firebase
import com.google.firebase.storage.storage
import com.squareup.picasso.Picasso

class AdminWineAdapter(
    private var wineList: List<WineModel>,
    private val listener: OnAdminWineClickListener
) : RecyclerView.Adapter<AdminWineAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: AdminWineCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            // Set click listener for expanding/collapsing details
            binding.headerLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    binding.expandableLayout.visibility =
                        if (binding.expandableLayout.visibility == View.VISIBLE)
                            View.GONE else View.VISIBLE
                }
            }

            // Set click listeners for admin actions
            binding.btnEdit.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onEditClick(wineList[position])
                }
            }

            binding.btnDelete.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onDeleteClick(wineList[position])
                }
            }
        }


        fun bind(wine: WineModel) {
            // Bind header information
            binding.txtWineName.text = wine.name
            binding.txtStock.text = "Stock: ${wine.stock}"

            // Bind wine card details (when expanded)
            binding.wineCard.apply {
                // Load wine image from Firebase
                val storageRef = Firebase.storage.reference.child(wine.image_path)
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get()
                        .load(uri.toString())
                        .error(R.drawable.wine_bottle_t)
                        .into(imgBottle)
                }.addOnFailureListener {
                    imgBottle.setImageResource(R.drawable.wine_bottle_t)
                }

                // Set wine card details
                txtWineName.text = wine.name
                txtWineProcuder.text = wine.producer
                txtWineCountry.text = wine.country
                txtPrice.text = String.format("$%.2f", wine.sale_price)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = AdminWineCardBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(wineList[position])
    }

    override fun getItemCount() = wineList.size
    fun updateData(newWineList: List<WineModel>) {
        wineList = newWineList
        notifyDataSetChanged()
    }


}
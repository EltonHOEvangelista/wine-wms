package com.example.winewms.ui.control.manageWines

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.AdminWineCardBinding
import com.example.winewms.ui.control.manageWines.OnAdminWineClickListener
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
            binding.headerLayout.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    binding.expandableLayout.visibility =
                        if (binding.expandableLayout.visibility == View.VISIBLE)
                            View.GONE else View.VISIBLE
                }
            }

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
            binding.apply {
                // Reset views to initial state
                expandableLayout.visibility = View.GONE
                imgBottle.setImageResource(R.drawable.wine_bottle_t)

                // Update text data
                txtWineName.text = wine.name
                txtProducerCountry.text = "${wine.producer} | ${wine.country}"
                txtPrice.text = String.format("$%.2f", wine.sale_price)
                txtStock.text = "Stock: ${wine.stock}"
                txtWineDescription.text = wine.description

                // Load image from Firebase
                val storageRef = Firebase.storage.reference
                val imageRef = storageRef.child(wine.image_path)

                // Get download URL and load with Picasso
                imageRef.downloadUrl
                    .addOnSuccessListener { uri ->
                        Picasso.get()
                            .load(uri)
                            .placeholder(R.drawable.wine_bottle_t)
                            .error(R.drawable.wine_bottle_t)
                            .into(imgBottle)
                    }
                    .addOnFailureListener {
                        imgBottle.setImageResource(R.drawable.wine_bottle_t)
                    }




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
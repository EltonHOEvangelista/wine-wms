package com.example.winewms.ui.control.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.WineModel
import com.example.winewms.databinding.AdminWineCardBinding
import com.example.winewms.ui.control.adapter.OnAdminWinesClickListener

class AdminWinesAdapter(
    private var wineList: List<WineModel>,
    private val listener: OnAdminWinesClickListener
) : RecyclerView.Adapter<AdminWinesAdapter.AdminWineViewHolder>() {

    private var expandedPosition = -1 // Track which card is expanded

    inner class AdminWineViewHolder(private val binding: AdminWineCardBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(wine: WineModel, position: Int) {
            val isExpanded = position == expandedPosition
            binding.linearLayoutDetails.visibility = if (isExpanded) View.VISIBLE else View.GONE

            binding.apply {
                txtWineName.text = wine.name
                txtWineStock.text = "Stock: ${wine.stock}"
                txtPrice.text = String.format("Price: $%.2f", wine.price)

                btnEditStock.setOnClickListener {
                    val newStock = edtStock.text.toString().toIntOrNull()
                    if (newStock != null) listener.onEditStockClick(wine, newStock)
                }

                btnDelete.setOnClickListener { listener.onDeleteClick(wine) }
            }

            itemView.setOnClickListener {
                expandedPosition = if (isExpanded) -1 else position
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminWineViewHolder {
        val binding = AdminWineCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AdminWineViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminWineViewHolder, position: Int) {
        holder.bind(wineList[position], position)
    }

    override fun getItemCount(): Int = wineList.size

    fun updateWineList(newWineList: List<WineModel>) {
        this.wineList = newWineList
        notifyDataSetChanged()
    }
}

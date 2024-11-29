package com.example.winewms.ui.control.reports.sales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R


class SalesReportAdapter(
    private var soldWines: List<SoldWine>, // Lista de vinhos vendidos
    private val onItemClick: (SoldWine) -> Unit // Callback para clique no vinho
) : RecyclerView.Adapter<SalesReportAdapter.SoldWineViewHolder>() {

    // ViewHolder para vinhos vendidos
    class SoldWineViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvWineName: TextView = view.findViewById(R.id.tvWineName)
        private val tvQuantitySold: TextView = view.findViewById(R.id.tvQuantitySold)

        fun bind(wine: SoldWine, onItemClick: (SoldWine) -> Unit) {
            tvWineName.text = wine.wineName
            tvQuantitySold.text = "Sold: ${wine.quantitySold}"
            itemView.setOnClickListener { onItemClick(wine) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoldWineViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.item_sold_wine, parent, false)
        return SoldWineViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoldWineViewHolder, position: Int) {
        val wine = soldWines[position]
        holder.bind(wine, onItemClick)
    }

    override fun getItemCount(): Int = soldWines.size

    // update sold wines
    fun updateData(newSoldWines: List<SoldWine>) {
        soldWines = newSoldWines
        notifyDataSetChanged()
    }



}


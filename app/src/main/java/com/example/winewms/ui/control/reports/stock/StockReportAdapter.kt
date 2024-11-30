package com.example.winewms.ui.control.reports.stock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R

class StockReportAdapter(
    private var stockItems: List<StockItem>
) : RecyclerView.Adapter<StockReportAdapter.StockViewHolder>() {


    class StockViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val wineName: TextView = view.findViewById(R.id.tvWineName)
        private val totalStock: TextView = view.findViewById(R.id.tvStock)
        private val salePrice: TextView = view.findViewById(R.id.tvSalePrice)
        private val locations: TextView = view.findViewById(R.id.tvLocations)


        fun bind(stockItem: StockItem) {
            wineName.text = stockItem.name
            totalStock.text = "Stock: ${stockItem.stock}"
            salePrice.text = "Price: $${String.format("%.2f", stockItem.salePrice)}"
            locations.text = stockItem.warehouse.let {
                "Location: ${it.location}, Aisle: ${it.aisle}, Shelf: ${it.shelf}"
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_stock_report, parent, false)
        return StockViewHolder(view)
    }

    override fun onBindViewHolder(holder: StockViewHolder, position: Int) {
        holder.bind(stockItems[position])
    }

    override fun getItemCount(): Int = stockItems.size


    fun updateData(newStockItems: List<StockItem>) {
        stockItems = newStockItems
        notifyDataSetChanged()
    }
}

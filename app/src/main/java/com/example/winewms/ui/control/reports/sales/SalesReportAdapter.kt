package com.example.winewms.ui.control.reports.sales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.R


class SalesReportAdapter(
    private var salesReport: SalesReportModel?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // View types to differentiate between categories and best sellers
    companion object {
        private const val VIEW_TYPE_CATEGORY = 1
        private const val VIEW_TYPE_BEST_SELLER = 2
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_CATEGORY -> {
                val view = inflater.inflate(R.layout.item_sales_category, parent, false)
                SalesCategoryViewHolder(view)
            }
            VIEW_TYPE_BEST_SELLER -> {
                val view = inflater.inflate(R.layout.item_best_seller, parent, false)
                BestSellerViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (position < (salesReport?.salesByCategory?.size ?: 0)) {
            VIEW_TYPE_CATEGORY
        } else {
            VIEW_TYPE_BEST_SELLER
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        salesReport?.let {
            if (holder is SalesCategoryViewHolder) {
                val category = it.salesByCategory.keys.elementAt(position)
                val sales = it.salesByCategory[category] ?: 0.0
                holder.bind(category, sales)
            } else if (holder is BestSellerViewHolder) {
                val bestSellerIndex = position - (it.salesByCategory.size)
                val bestSeller = it.bestSellingProducts[bestSellerIndex]
                holder.bind(bestSeller)
            }
        }
    }

    override fun getItemCount(): Int {
        val categoriesCount = salesReport?.salesByCategory?.size ?: 0
        val bestSellersCount = salesReport?.bestSellingProducts?.size ?: 0
        return categoriesCount + bestSellersCount
    }

    // ViewHolder for sales categories
    class SalesCategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvCategory: TextView = view.findViewById(R.id.tvCategory)
        private val tvCategorySales: TextView = view.findViewById(R.id.tvCategorySales)

        fun bind(category: String, sales: Double) {
            tvCategory.text = category
            tvCategorySales.text = "$${"%.2f".format(sales)}"
        }
    }

    // ViewHolder for best-selling products
    class BestSellerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val tvProductName: TextView = view.findViewById(R.id.tvProductName)
        private val tvUnitsSold: TextView = view.findViewById(R.id.tvUnitsSold)
        private val tvTotalRevenue: TextView = view.findViewById(R.id.tvTotalRevenue)

        fun bind(bestSeller: BestSellingProduct) {
            tvProductName.text = bestSeller.productName
            tvUnitsSold.text = "Units: ${bestSeller.totalUnitsSold}"
            tvTotalRevenue.text = "$${"%.2f".format(bestSeller.totalRevenue)}"
        }
    }

    // Update the data in the adapter
    fun updateData(newReport: SalesReportModel?) {
        salesReport = newReport
        notifyDataSetChanged()
    }
}

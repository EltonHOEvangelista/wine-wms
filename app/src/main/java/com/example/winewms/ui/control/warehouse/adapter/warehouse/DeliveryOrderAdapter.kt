package com.example.winewms.ui.control.warehouse.adapter.warehouse

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.SoldItems
import com.example.winewms.data.model.WineInvoice
import com.example.winewms.databinding.DeliveryOrderCardBinding

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

class DeliveryOrderAdapter(
    private val invoiceList: List<WineInvoice>,
    private val listener: onDeliveryOrderClickListener,
    var context: Context?
) : RecyclerView.Adapter<DeliveryOrderAdapter.DeliveryOrderViewHolder>() {

    //variable to handle selected recyclerView position.
    private var selectedIndex: Int = -1

    // ViewHolder class holds the views for each item in the list.
    inner class DeliveryOrderViewHolder(val binding: DeliveryOrderCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Pass the selected wine to the listener
                    listener.onDeliveryOrderClickListener(invoiceList[position])
                }

                if (binding.linearLayoutDeliveryOrderHeader.visibility == View.VISIBLE) {
                    binding.linearLayoutDeliveryOrderDetails.visibility = View.VISIBLE
                    binding.linearLayoutDeliveryOrderHeader.visibility = View.GONE
                }
                else {
                    binding.linearLayoutDeliveryOrderDetails.visibility = View.GONE
                    binding.linearLayoutDeliveryOrderHeader.visibility = View.VISIBLE
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeliveryOrderViewHolder {
        val binding = DeliveryOrderCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DeliveryOrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DeliveryOrderViewHolder, position: Int) {

        // Gets the current item from the list
        val item = invoiceList[position]

        //Load following invoice data
        val unknown = "unknown"
        holder.binding.txtDeliveryOrderInvoiceId.text = item.invoiceId  ?: unknown
        holder.binding.txtDeliveryOrderInvoiceDate.text = item.salesDate.take(10)  ?: unknown

        holder.binding.txtBillTo.text = item.accountId

        holder.binding.txtShipTo.text = """
            ${item.shippingAddress.address}, 
            ${item.shippingAddress.city}, 
            ${item.shippingAddress.province}, 
            ${item.shippingAddress.postalCode}
        """.trimIndent()

        holder.binding.txtInvoiceNumber.text = """
            # ${item.invoiceId}, 
            Date ${item.salesDate.take(10)}
        """.trimIndent()

        val linearLayoutHeader = setupDeliveryOrderHeader()
        holder.binding.linearLayoutWines.addView(linearLayoutHeader)

        for (wine in item.items) {
            val linearLayout = setupInvoiceWine(wine)
            holder.binding.linearLayoutWines.addView(linearLayout)
        }
    }

    private fun setupDeliveryOrderHeader(): LinearLayout {

        // Create a LinearLayout
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )
            setPadding(0, 4.dp, 0, 4.dp)
        }

        // Function to create a vertical divider
        fun createDivider(): View {
            return View(context).apply {
                layoutParams = LinearLayout.LayoutParams(1.dp, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    setBackgroundColor(Color.GRAY)
                }
            }
        }

        // Create and configure the wine quantity TextView
        val wineQtd = TextView(context).apply {
            text = "Qty"
            layoutParams = LinearLayout.LayoutParams(50.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.1f
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the wine name TextView
        val wineName = TextView(context).apply {
            text = "Description"
            layoutParams = LinearLayout.LayoutParams(210.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.3f
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the aisle TextView
        val aisle = TextView(context).apply {
            text = "Location"
            layoutParams = LinearLayout.LayoutParams(60.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.6f
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Add views and dividers to the LinearLayout
        linearLayout.addView(createDivider())
        linearLayout.addView(wineQtd)
        linearLayout.addView(wineName)
        linearLayout.addView(aisle)
        linearLayout.addView(createDivider())

        return linearLayout
    }

    // Function to setup invoice data
    private fun setupInvoiceWine(item: SoldItems): LinearLayout {

        // Create a LinearLayout
        val linearLayout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            setPadding(0, 4.dp, 0, 4.dp)
        }

        // Function to create a vertical divider
        fun createDivider(): View {
            return View(context).apply {
                layoutParams = LinearLayout.LayoutParams(1.dp, LinearLayout.LayoutParams.MATCH_PARENT).apply {
                    //setMargins(4.dp, 0, 4.dp, 0) // Optional margin for better spacing
                    setBackgroundColor(Color.GRAY)
                }
            }
        }

        // Create and configure the wine quantity TextView
        val wineQtd = TextView(context).apply {
            text = item.quantity.toString()
            layoutParams = LinearLayout.LayoutParams(50.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.1f
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the wine name TextView
        val wineName = TextView(context).apply {
            text = item.name
            layoutParams = LinearLayout.LayoutParams(210.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.3f
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the aisle TextView
        val aisle = TextView(context).apply {
            var location: String = ""
            for (position in item.stockLocation) {
                location += "${position}\n"
            }
            text = location
            layoutParams = LinearLayout.LayoutParams(210.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                weight = 0.6f
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }


        // Add views and dividers to the LinearLayout
        linearLayout.addView(createDivider())
        linearLayout.addView(wineQtd)
        linearLayout.addView(createDivider())
        linearLayout.addView(wineName)
        linearLayout.addView(createDivider())
        linearLayout.addView(aisle)
        linearLayout.addView(createDivider())

        return linearLayout
    }


    // Returns the total number of items in the list.
    override fun getItemCount() = invoiceList.size

    //function to set selected recyclerView position
    fun setSelectedIndex(selectedIndex: Int) {
        this.selectedIndex = selectedIndex
        notifyDataSetChanged()
    }
}
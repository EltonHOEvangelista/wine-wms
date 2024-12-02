package com.example.winewms.ui.account.invoices

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.winewms.data.model.SoldItems
import com.example.winewms.data.model.WineInvoice
import com.example.winewms.databinding.InvoiceCardBinding

val Int.dp: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()

class InvoicesAdapter(
    private val invoiceList: List<WineInvoice>,
    private val listener: onInvoicesClickListener,
    var context: Context?,
    var firstName: String,
    var lastName: String,
) : RecyclerView.Adapter<InvoicesAdapter.InvoiceViewHolder>() {

    //variable to handle selected recyclerView position.
    private var selectedIndex: Int = -1

    // ViewHolder class holds the views for each item in the list.
    inner class InvoiceViewHolder(val binding: InvoiceCardBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    // Pass the selected wine to the listener
                    listener.onInvoicesClickListener(invoiceList[position])
                }

                if (binding.linearLayoutInvoiceHeader.visibility == View.VISIBLE) {
                    binding.linearLayoutInvoiceDetails.visibility = View.VISIBLE
                    binding.linearLayoutInvoiceHeader.visibility = View.GONE
                }
                else {
                    binding.linearLayoutInvoiceDetails.visibility = View.GONE
                    binding.linearLayoutInvoiceHeader.visibility = View.VISIBLE
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvoiceViewHolder {
        val binding = InvoiceCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return InvoiceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InvoiceViewHolder, position: Int) {

        // Gets the current item from the list
        val item = invoiceList[position]

        //Load following invoice data
        val unknown = "unknown"
        holder.binding.txtInvoiceId.text = item.invoiceId  ?: unknown
        holder.binding.txtInvoiceDate.text = item.salesDate.take(10)  ?: unknown

        holder.binding.txtBillTo.text = "${lastName}, ${firstName}"

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

        val linearLayoutHeader = setupInvoiceHeader()
        holder.binding.linearLayoutWines.addView(linearLayoutHeader)

        for (wine in item.items) {
            val linearLayout = setupInvoiceWine(wine)
            holder.binding.linearLayoutWines.addView(linearLayout)
        }

        holder.binding.txtInvoiceSubTotal.text = item.totalPrice.toString()
        holder.binding.txtGST.text = String.format(" $%.2f", item.totalPrice * 0.05)
        holder.binding.txtPST.text = String.format(" $%.2f", item.totalPrice * 0.10)
        holder.binding.txtInvoiceTotal.text = String.format(" $%.2f", item.totalPrice +
                item.totalPrice * 0.05 +
                item.totalPrice * 0.10)
    }

    private fun setupInvoiceHeader(): LinearLayout {

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
                    setBackgroundColor(Color.GRAY)
                }
            }
        }

        // Create and configure the wine quantity TextView
        val wineQtd = TextView(context).apply {
            text = "Qty"
            layoutParams = LinearLayout.LayoutParams(50.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the wine name TextView
        val wineName = TextView(context).apply {
            text = "Description"
            layoutParams = LinearLayout.LayoutParams(210.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the unit price TextView
        val unitPrice = TextView(context).apply {
            text = "Unit Price"
            layoutParams = LinearLayout.LayoutParams(60.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the total price TextView
        val amountPrice = TextView(context).apply {
            text = "Amount"
            layoutParams = LinearLayout.LayoutParams(60.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
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
        linearLayout.addView(unitPrice)
        linearLayout.addView(createDivider())
        linearLayout.addView(amountPrice)
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
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the wine name TextView
        val wineName = TextView(context).apply {
            text = item.name
            layoutParams = LinearLayout.LayoutParams(210.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the unit price TextView
        val unitPrice = TextView(context).apply {
            text = String.format(" $%.2f", item.finalUnitPrice)
            layoutParams = LinearLayout.LayoutParams(60.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
                gravity = Gravity.CENTER
            }
            textSize = 12f
            gravity = Gravity.CENTER
        }

        // Create and configure the total price TextView
        val amountPrice = TextView(context).apply {
            text = String.format(" $%.2f", item.itemTotal)
            layoutParams = LinearLayout.LayoutParams(60.dp, LinearLayout.LayoutParams.WRAP_CONTENT).apply {
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
        linearLayout.addView(unitPrice)
        linearLayout.addView(createDivider())
        linearLayout.addView(amountPrice)
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
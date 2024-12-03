package com.example.winewms.data.model

import android.os.Parcelable
import com.example.winewms.ui.account.AccountAddressModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SalesDataWrapper(
    val invoices: List<WineInvoice>,
    @SerializedName("sale_refused") val saleRefused: List<SalesRefused>
) : Parcelable

@Parcelize
data class WineInvoice(
    @SerializedName("_id") val invoiceId: String,
    @SerializedName("account_id") val accountId: String,
    val items: List<SoldItems>,
    @SerializedName("total_price") val totalPrice: Double,
    @SerializedName("sales_date") val salesDate: String,
    @SerializedName("shipping_address") val shippingAddress: AccountAddressModel
) : Parcelable

@Parcelize
data class SoldItems(
    @SerializedName("wine_id") val wineId: String,
    val name: String,
    @SerializedName("sale_price") val salePrice: Double,
    val discount: Double,
    @SerializedName("final_price_per_unit") val finalUnitPrice: Double,
    val quantity: Int,
    @SerializedName("item_total") val itemTotal: Double,
    @SerializedName("stock_location") val stockLocation: List<String>
) : Parcelable

@Parcelize
data class SalesRefused(
    @SerializedName("wine_id") val wineId: String,
    @SerializedName("available_stock") val availableStock: Int,
    @SerializedName("requested_quantity") val requestedQuantity: Int
) : Parcelable

@Parcelize
class CumulativeSales(
    val date: String,
    @SerializedName("cumulative_sales_unit") val cumulativeSalesUnit: String,
    @SerializedName("cumulative_sales_amount") val cumulativeSalesAmount: String
) : Parcelable
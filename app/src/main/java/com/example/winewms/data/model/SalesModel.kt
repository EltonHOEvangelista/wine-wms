package com.example.winewms.data.model

import android.os.Parcelable
import com.example.winewms.ui.account.AccountAddressModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SalesModel(
    @SerializedName("_id") val invoiceId: String,
    val items: List<SalesItem>,
    val account: SalesAccount,
    @SerializedName("shipping_address") val shippingAddress: AccountAddressModel
) : Parcelable

@Parcelize
data class SalesItem(
    @SerializedName("wine_id") val wineId: String,
    val quantity: Int
) : Parcelable

@Parcelize
data class SalesAccount(
    @SerializedName("account_id") val accountId: String,
    val email: String,
    val phone: String,
) : Parcelable


@Parcelize
data class SalesRequestDataWrapper(
    var sales: List<SalesRequestModel>
) : Parcelable

@Parcelize
data class SalesRequestModel(
    val account_id: String,
    val items: List<SalesRequestItem>,
    val total_price: Double,
    val sales_date: String,
    val shipping_address: ShippingAddressModel,
    val dispatch_status: String
) : Parcelable

@Parcelize
data class SalesRequestItem(
    val wine_id: String,
    val name: String,
    val sale_price: Double,
    val discount: Double,
    val final_price_per_unit: Double,
    val quantity: Int,
    val item_total: Double,
    val stock_location: List<StockRequestLocation>
) : Parcelable

@Parcelize
data class StockRequestLocation(
    val warehouse_id: String,
    val aisle: String,
    val shelf: String
) : Parcelable

@Parcelize
data class ShippingAddressModel (
    val address: String,
    val city: String,
    val country: String,
    val province: String,
    val postalCode: String
) : Parcelable

//@Parcelize
//data class SalesRequestModel(
//    @SerializedName("_id") val invoiceId: String,
//    @SerializedName("account_id") val accountId: String,
//    val items: List<SalesRequestItem>,
//    @SerializedName("total_price") val totalPrice: Double,
//    @SerializedName("sales_date") val salesDate: String,
//    @SerializedName("shipping_address") val shippingAddress: AccountAddressModel,
//    @SerializedName("dispatch_status") val dispatchStatus: String
//) : Parcelable
//
//@Parcelize
//data class SalesRequestItem(
//    @SerializedName("wine_id") val wineId: String,
//    val name: String,
//    @SerializedName("sale_price") val salePrice: Double,
//    val discount: Double,
//    @SerializedName("final_price_per_unit") val finalPricePerUnit: Double,
//    val quantity: Int,
//    @SerializedName("item_total") val itemTotal: Double,
//    @SerializedName("stock_location") val stockLocation: List<StockRequestLocation>,
//) : Parcelable
//
//@Parcelize
//data class StockRequestLocation(
//    @SerializedName("warehouse_id") val warehouseId: String,
//    val aisle: String,
//    val shelf: String,
//) : Parcelable
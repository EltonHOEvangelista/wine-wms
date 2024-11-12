package com.example.winewms.data.model.payloads

import com.google.gson.annotations.SerializedName
import java.util.*

data class PurchaseResponse(
    @SerializedName("id") val id: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("items") val items: List<PurchaseItem>,
    @SerializedName("totalPrice") val totalPrice: Double,
    @SerializedName("purchaseDate") val purchaseDate: Date,  // Using Date type here
    @SerializedName("address") val address: String,
    @SerializedName("city") val city: String,
    @SerializedName("province") val province: String,
    @SerializedName("postalCode") val postalCode: String
)

data class PurchaseItemResponse(
    @SerializedName("wine_id") val wineId: String,
    val name: String,
    val price: Double,
    val discount: Double,
    @SerializedName("final_price_per_unit") val finalPricePerUnit: Double,
    val quantity: Int,
    @SerializedName("item_total") val itemTotal: Double
)

data class InsufficientStockItem(
    @SerializedName("wine_id") val wineId: String,
    @SerializedName("available_stock") val availableStock: Int,
    @SerializedName("requested_quantity") val requestedQuantity: Int
)

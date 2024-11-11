package com.example.winewms.data.model.payloads

import com.google.gson.annotations.SerializedName

data class PurchaseRequest(
    @SerializedName("user_id") val userId: String,
    val items: List<PurchaseItem>,
    val address: String?,
    val city: String?,
    val province: String?,
    @SerializedName("postal_code") val postalCode: String?
)

data class PurchaseItem(
    @SerializedName("wine_id") val wineId: String,
    val quantity: Int
)

package com.example.winewms.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import com.google.gson.annotations.SerializedName

@Parcelize
data class PurchaseDataWrapper(
    var page: Int,
    var limit: Int,
    var total_count: Int,
    var total_pages: Int,
    var purchases: List<PurchaseModel>
) : Parcelable

@Parcelize
data class PurchaseModel(
    @SerializedName("_id") var id: String,
    var wine_id: String,
    var cost_price: Float,
    var amount: Int,
    var date: String
) : Parcelable
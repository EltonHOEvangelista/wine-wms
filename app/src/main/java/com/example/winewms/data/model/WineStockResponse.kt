package com.example.winewms.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WineStockResponse(
    val wine_id: String,
    val total_stock: Int
) : Parcelable

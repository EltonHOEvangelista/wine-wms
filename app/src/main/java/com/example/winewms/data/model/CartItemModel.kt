package com.example.winewms.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItemModel(
    val wine: WineModel,
    var quantity: Int = 1,
    var stockNotification: String? = null
) : Parcelable

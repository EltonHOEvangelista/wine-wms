package com.example.winewms.data.model

import android.os.Parcelable
import com.example.winewms.ui.account.AccountAddressModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class SalesModel(
    @SerializedName("_id") val invoiceId: String,
    val items: List<SalesItem>,
    val account: SalesAccount
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
    val address: AccountAddressModel
) : Parcelable
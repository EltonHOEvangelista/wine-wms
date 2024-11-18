package com.example.winewms.ui.account

import android.os.Parcelable
import com.example.winewms.data.model.WineModel
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class AccountDataWrapper(
    var message: String,
    @SerializedName("request_status") var requestStatus: Boolean,
    @SerializedName("_account") var accountModel: AccountModel
) : Parcelable

@Parcelize
data class AccountModel(
    @SerializedName("_id") val accountId: String,
    @SerializedName("first_name") val firstName: String,
    @SerializedName("last_name") val lastName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phone: String,
    val status: Int,
    val type: Int,
    val address: AccountAddressModel
) : Parcelable

@Parcelize
data class AccountAddressModel (
    val address: String,
    val city: String,
    val province: String,
    val postalCode: String,
    val country: String,
) : Parcelable
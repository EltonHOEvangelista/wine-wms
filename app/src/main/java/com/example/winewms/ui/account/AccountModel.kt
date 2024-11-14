package com.example.winewms.ui.account


data class AccountModel(
    val accountId: Int,
    val firstName: String,
    val lastName: String,
    val email: String,
    val password: String,
    val confirmPassword: String,
    val phone: String,
    val accountStatus: Int,
    val accountType: Int,
    val address: AccountAddressModel? = null

)

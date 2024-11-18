package com.example.winewms.ui.account.signin

import android.os.Parcelable
import com.example.winewms.ui.account.AccountModel
import kotlinx.parcelize.Parcelize

@Parcelize
data class SigninModel(
    var email: String,
    var password: String
) : Parcelable
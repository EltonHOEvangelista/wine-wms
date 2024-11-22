package com.example.winewms.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ResponseModel(
    var response_id: String,
    var message: String,
    @SerializedName("response_status") var responseStatus: Boolean
) : Parcelable
package com.example.winewms.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FinancialDataWrapper(
    @SerializedName("financial_data") val financialData: List<CumulativeCostRevenue>
) : Parcelable

@Parcelize
class CumulativeCostRevenue(
    val date: String,
    @SerializedName("cumulative_cost") val cumulativeCost: String,
    @SerializedName("cumulative_sales") val cumulativeSales: String
    ) : Parcelable
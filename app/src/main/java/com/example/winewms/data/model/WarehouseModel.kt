package com.example.winewms.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class WarehouseDataWrapper(
    var page: Int,
    var limit: Int,
    var total_count: Int,
    var total_pages: Int,
    var warehouses: List<WarehouseModel>
) : Parcelable

@Parcelize
data class WarehouseModel(
    @SerializedName("_id") var id: String,
    var location: String,
    var aisles: List<Aisle>
) : Parcelable

@Parcelize
data class Aisle(
    var aisle: String,
    var shelves: List<Shelf>
) : Parcelable

@Parcelize
data class Shelf(
    var shelf: String,
    var wines: List<WineBox>
) : Parcelable

@Parcelize
data class WineBox(
    var wine_id: String,
    var stock: Int
) : Parcelable
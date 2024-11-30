package com.example.winewms.data.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataWrapper(
    var page: Int,
    var limit: Int,
    var total_count: Int,
    var total_pages: Int,
    var wines: List<WineModel>
) : Parcelable

@Parcelize
data class WineModel(
    @SerializedName("_id") var id: String,
    var image_path: String,
    var name: String,
    var producer: String,
    var country: String,
    var harvest_year: Int,
    var type: String,
    var rate: Float,
    var description: String,
    var reviews: List<String>,
    var grapes: List<String>,
    var taste_characteristics: TasteCharacteristics,
    var food_pair: List<String>,
    var sale_price: Float,
    var discount: Float,
    var stock: Int
) : Parcelable

@Parcelize
data class TasteCharacteristics(
    var lightness: Int,
    var tannin: Int,
    var dryness: Int,
    var acidity: Int
) : Parcelable
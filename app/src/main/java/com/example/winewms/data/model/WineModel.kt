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
    var type: String,
    var grapes: List<String>,
    var country: String,
    var harvest: Int,
    var description: String,
    var price: Float,
    var discount: Float,
    var stock: Int = 0,
    var taste_characteristics: TasteCharacteristics,
    var rate: Float,
    var food_pair: List<String>,
    var reviews: List<String>,
) : Parcelable

@Parcelize
data class TasteCharacteristics(
    var lightness: Int,
    var tannin: Int,
    var dryness: Int,
    var acidity: Int,
) : Parcelable
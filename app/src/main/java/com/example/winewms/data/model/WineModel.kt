package com.example.winewms.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class DataWrapper(
    var wines: List<WineModel>
) : Parcelable

@Parcelize
data class WineModel(
    var id: String,
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
    var stock: Int,
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
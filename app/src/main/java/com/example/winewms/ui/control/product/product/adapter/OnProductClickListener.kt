package com.example.winewms.ui.control.product.product.adapter

import com.example.winewms.data.model.WineModel

interface OnProductClickListener {
    fun OnProductClickListener(wineModel: WineModel)
    fun onEditClickListener(wineModel: WineModel)
    fun onDetailsClickListener(wineModel: WineModel)
}
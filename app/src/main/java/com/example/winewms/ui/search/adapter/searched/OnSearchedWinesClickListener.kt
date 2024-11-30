package com.example.winewms.ui.search.adapter.searched

import com.example.winewms.data.model.WineModel

interface OnSearchedWinesClickListener {
    fun onSearchedWinesClickListener(wineModel: WineModel)
    fun onBuyClick(wineModel: WineModel)
    fun onEditClick(wineModel: WineModel)
    fun onDeleteClick(wineModel: WineModel)
    fun onAddClick(wineModel: WineModel)
    fun onDetailsClick(wineModel: WineModel)
}
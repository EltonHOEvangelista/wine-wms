package com.example.winewms.ui.control.adapter

import com.example.winewms.data.model.WineModel

interface OnAdminWinesClickListener {
    fun onEditStockClick(wine: WineModel, newStock: Int)
    fun onDeleteClick(wine: WineModel)

}

package com.example.winewms.ui.control

import com.example.winewms.data.model.WineModel

interface OnAdminWineClickListener {
    fun onEditClick(wineModel: WineModel)
    fun onDeleteClick(wineModel: WineModel)
}
package com.example.winewms.ui.cart.adapter.cart

import com.example.winewms.data.model.CartItemModel

interface OnCartWinesClickListener {
    fun onCartWinesClickListener(model: CartItemModel)
    fun onIncreaseQuantityClick(wineModel: CartItemModel)
    fun onDecreaseQuantityClick(wineModel: CartItemModel)
    fun onRemoveItemClick(wineModel: CartItemModel)
}
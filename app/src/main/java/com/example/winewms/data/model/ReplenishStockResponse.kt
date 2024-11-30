package com.example.winewms.data.model

data class ReplenishStockResponse(
    val message: String,
    val new_stock_quantity: Int,
    val purchase_id: String
)


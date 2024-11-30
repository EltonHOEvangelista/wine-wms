package com.example.winewms.ui.control.reports.stock

data class StockItem(
    val wineId: String,
    val name: String,
    val stock: Int,
    val salePrice: Double,
    val rate: Double,
    val warehouse: Warehouse
)

data class Warehouse(
    val location: String,
    val aisle: String,
    val shelf: String
)
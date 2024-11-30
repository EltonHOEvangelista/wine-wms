package com.example.winewms.ui.control.reports.stock

data class StockItem(
    val wineId: String,
    val wineName: String,
    val totalStock: Int,
    val locations: List<Location>
)

data class Location(
    val warehouseId: String,
    val aisle: String,
    val shelf: String,
    val stock: Int
)

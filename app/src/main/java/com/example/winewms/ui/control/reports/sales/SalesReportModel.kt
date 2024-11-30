package com.example.winewms.ui.control.reports.sales

data class SalesReportModel(
    val totalSales: Double,
    val unitsSold: Int,
    val salesByCategory: Map<String, Double>,
    val bestSellingProducts: List<BestSellingProduct>
)

data class BestSellingProduct(
    val productId: String,
    val productName: String,
    val totalUnitsSold: Int,
    val totalRevenue: Double
)

data class SalesComparison(
    val currentMonthSales: Float,
    val previousMonthSales: Float
)

data class MonthlySales(
    val totalSales: Float,
    val unitsSold: Int
)

data class SoldWine(
    val wineId: String,
    val wineName: String,
    val quantitySold: Int,
    val saleDate: String,
    val salePrice: Double? = null,
    val rate: Double? = null
)


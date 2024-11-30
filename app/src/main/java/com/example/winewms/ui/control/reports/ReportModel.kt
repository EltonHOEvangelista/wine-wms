package com.example.winewms.ui.control.reports

data class ReportModel(
    val totalSales: Double,
    val unitsSold: Int,
    val salesByCategory: Map<String, Double>,
    val bestSellingProducts: List<ProductReport>
)

data class ProductReport(
    val productId: String,
    val name: String,
    val unitsSold: Int,
    val revenue: Double
)

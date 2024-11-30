package com.example.winewms.ui.control.reports.financial


data class FinanceReportModel(
    val totalSales: Double,
    val totalPurchases: Double,
    val totalBalance: Double,
    val startDate: String,
    val endDate: String
)
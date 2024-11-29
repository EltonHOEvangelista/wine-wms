package com.example.winewms.ui.control.reports.financial


data class FinanceReportModel(
    val totalSales: Double,        // Total de vendas no período
    val totalPurchases: Double,    // Total de compras no período
    val totalBalance: Double       // Saldo financeiro (vendas - compras)
)
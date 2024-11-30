package com.example.winewms.ui.control.reports


import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.ui.control.reports.financial.FinanceReportModel
import com.example.winewms.ui.control.reports.sales.BestSellingProduct
import com.example.winewms.ui.control.reports.sales.SalesComparison
import com.example.winewms.ui.control.reports.sales.SalesReportModel
import com.example.winewms.ui.control.reports.sales.SoldWine
import com.example.winewms.ui.control.reports.stock.StockItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import kotlinx.coroutines.launch

class ReportsViewModel : ViewModel() {

    private val _salesComparison = MutableLiveData<SalesComparison?>()
    val salesComparison: LiveData<SalesComparison?> = _salesComparison

    private val _salesReport = MutableLiveData<SalesReportModel?>()
    val salesReport: LiveData<SalesReportModel?> = _salesReport

    private val _soldWines = MutableLiveData<List<SoldWine>>()
    val soldWines: LiveData<List<SoldWine>> = _soldWines

    private val _stockReport = MutableLiveData<List<StockItem>>()
    val stockReport: LiveData<List<StockItem>> = _stockReport

    private val _lowStockWines = MutableLiveData<List<StockItem>>()
    val lowStockWines: LiveData<List<StockItem>> = _lowStockWines

    private val _financeReport = MutableLiveData<FinanceReportModel?>()
    val financeReport: LiveData<FinanceReportModel?> = _financeReport

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _filteredStockItems = MutableLiveData<List<StockItem>>()
    val filteredStockItems: LiveData<List<StockItem>> = _filteredStockItems

    private var isMockMode: Boolean = true


    private val apiService: WineApiService by lazy {
        WineApi.retrofit.create(WineApiService::class.java)
    }

    // Function to load mock data from JSON files
    private fun loadMockData(context: Context): Triple<List<StockItem>, List<SoldWine>, List<FinanceReportModel>> {
        return try {
            val winesJson = context.assets.open("wines.json").bufferedReader().use { it.readText() }
            val salesJson = context.assets.open("sales.json").bufferedReader().use { it.readText() }
            val financeJson = context.assets.open("finance.json").bufferedReader().use { it.readText() }

            val wines: List<StockItem> = Gson().fromJson(winesJson, object : TypeToken<List<StockItem>>() {}.type)
            val sales: List<SoldWine> = Gson().fromJson(salesJson, object : TypeToken<List<SoldWine>>() {}.type)
            val finance: List<FinanceReportModel> = Gson().fromJson(financeJson, object : TypeToken<List<FinanceReportModel>>() {}.type)

            Triple(wines, sales, finance)
        } catch (e: Exception) {
            Log.e("ReportsViewModel", "Error loading mock data: ${e.message}", e)
            Triple(emptyList(), emptyList(), emptyList())
        }
    }
    //SALES FUNCTIONS
    // Fetch sales comparison data
    fun fetchSalesComparison() {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                if (isMockMode) {
                    _salesComparison.postValue(SalesComparison(15000.0f, 12500.0f))
                } else {
                    val response = apiService.getSalesComparison().execute()
                    if (response.isSuccessful) {
                        _salesComparison.postValue(response.body())
                    } else {
                        Log.e("ReportsViewModel", "Failed to fetch sales comparison")
                        _salesComparison.postValue(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Error fetching sales comparison: ${e.message}", e)
                _salesComparison.postValue(null)
            } finally {
                _loading.postValue(false)
            }
        }
    }

    // Fetch sales report with optional filters
    fun fetchSalesReportWithFilters(
        context: Context,
        startDate: String? = null,
        endDate: String? = null,
        categories: List<String> = emptyList(),
        bestSellers: Boolean = false
    ) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                if (isMockMode) {
                    val report = loadMockSalesReport(context)


                    val filteredReport = report.copy(
                        bestSellingProducts = if (bestSellers) {
                            report.bestSellingProducts
                        } else {
                            report.bestSellingProducts.take(0)
                        }
                    )

                    _salesReport.postValue(filteredReport)
                } else {
                    val categoriesString = categories.joinToString(",")
                    val response = apiService.getSalesReportWithFilters(
                        startDate = startDate.orEmpty(),
                        endDate = endDate.orEmpty(),
                        categories = categoriesString,
                        bestSellers = bestSellers
                    ).execute()

                    if (response.isSuccessful) {
                        _salesReport.postValue(response.body())
                    } else {
                        Log.e("ReportsViewModel", "Failed to fetch sales report: ${response.errorBody()}")
                        _salesReport.postValue(null)
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Error fetching sales report: ${e.message}", e)
                _salesReport.postValue(null)
            } finally {
                _loading.postValue(false)
            }
        }
    }


    // Fetch sold wines
    fun fetchSoldWines(context: Context) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                if (isMockMode) {
                    val (wines, sales, _) = loadMockData(context)
                    val connectedSales = sales.mapNotNull { sale ->
                        val wine = wines.find { it.wineId == sale.wineId }
                        if (sale.wineId != null && wine != null) {
                            SoldWine(
                                wineId = sale.wineId,
                                wineName = wine.name ?: "Unknown",
                                quantitySold = sale.quantitySold,
                                saleDate = sale.saleDate,
                                salePrice = wine.salePrice ?: 0.0,
                                rate = wine.rate ?: 0.0
                            )
                        } else {
                            null // Ignora vendas ou vinhos inválidos
                        }
                    }
                    _soldWines.postValue(connectedSales)
                } else {
                    val response = apiService.getSoldWines()
                    _soldWines.postValue(response)
                }
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Error fetching sold wines: ${e.message}", e)
                _soldWines.postValue(emptyList())
            } finally {
                _loading.postValue(false)
            }
        }
    }


    private fun loadMockSalesReport(context: Context): SalesReportModel {
        return try {

            val salesJson = context.assets.open("sales.json").bufferedReader().use { it.readText() }
            val winesJson = context.assets.open("wines.json").bufferedReader().use { it.readText() }


            val sales: List<SoldWine> = Gson().fromJson(salesJson, object : TypeToken<List<SoldWine>>() {}.type)
            val wines: List<StockItem> = Gson().fromJson(winesJson, object : TypeToken<List<StockItem>>() {}.type)


            val salesByCategory = sales.groupBy { sale ->
                val wine = wines.find { it.wineId == sale.wineId }
                wine?.name ?: "Unknown"
            }.mapValues { entry ->
                entry.value.sumOf { it.quantitySold * it.salePrice!! }
            }


            val bestSellingProducts = sales.groupBy { it.wineId }.map { entry ->
                val wine = wines.find { it.wineId == entry.key }
                val totalUnitsSold = entry.value.sumOf { it.quantitySold }
                val totalRevenue = entry.value.sumOf { it.quantitySold * it.salePrice!! }

                BestSellingProduct(
                    productId = entry.key,
                    productName = wine?.name ?: "Unknown",
                    totalUnitsSold = totalUnitsSold,
                    totalRevenue = totalRevenue
                )
            }.sortedByDescending { it.totalUnitsSold }.take(5) // Top 5


            val totalSales = sales.sumOf { it.quantitySold * it.salePrice!! }
            val unitsSold = sales.sumOf { it.quantitySold }


            SalesReportModel(
                totalSales = totalSales,
                unitsSold = unitsSold,
                salesByCategory = salesByCategory,
                bestSellingProducts = bestSellingProducts
            )
        } catch (e: Exception) {
            Log.e("ReportsViewModel", "Error loading mock sales report: ${e.message}", e)
            SalesReportModel(
                totalSales = 0.0,
                unitsSold = 0,
                salesByCategory = emptyMap(),
                bestSellingProducts = emptyList()
            )
        }
    }

    //STOCK FUNCTIONS
    private fun loadMockStockReport(context: Context): List<StockItem> {
        return try {
            val json = context.assets.open("stock.json").bufferedReader().use { it.readText() }
            Gson().fromJson(json, object : TypeToken<List<StockItem>>() {}.type)
        } catch (e: Exception) {
            Log.e("ReportsViewModel", "Error loading stock.json: ${e.message}", e)
            emptyList()
        }
    }
//fetch Stock
    fun fetchStockReport(context: Context) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                if (isMockMode) {
                    _stockReport.postValue(loadMockStockReport(context))
                } else {
                    val response = apiService.getStockReport().execute()
                    if (response.isSuccessful) {
                        _stockReport.postValue(response.body())
                    } else {
                        _stockReport.postValue(emptyList())
                        Log.e("ReportsViewModel", "Failed to fetch stock report")
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Error fetching stock report: ${e.message}", e)
                _stockReport.postValue(emptyList())
            } finally {
                _loading.postValue(false)
            }
        }
    }

    // Fetch low stock wines
    fun fetchLowStockWines(context: Context, forceUpdate: Boolean = false) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                // Caso o flag forceUpdate seja verdadeiro, força a recarga dos itens de estoque baixo
                if (isMockMode) {
                    val (wines, _, _) = loadMockData(context)
                    val lowStock = wines.filter { it.stock < 10 }
                    _lowStockWines.postValue(lowStock)
                } else {
                    if (forceUpdate) {
                        val response = apiService.getLowStockWines().execute()
                        if (response.isSuccessful) {
                            _lowStockWines.postValue(response.body())
                        } else {
                            _lowStockWines.postValue(emptyList())
                            Log.e("ReportsViewModel", "Failed to fetch low stock wines")
                        }
                    } else {
                        _lowStockWines.postValue(emptyList()) // Quando não for necessário forçar a atualização, volta ao estado anterior
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Error fetching low stock wines: ${e.message}")
                _lowStockWines.postValue(emptyList())
            } finally {
                _loading.postValue(false)
            }
        }
    }
    //FINANCIAL FUNCTIONS
    // Fetch finance report
    fun fetchFinanceReport(context: Context, startDate: String, endDate: String) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                if (isMockMode) {
                    val (_, _, financeReports) = loadMockData(context)
                    val filteredReport = financeReports.find {
                        it.startDate == startDate && it.endDate == endDate // Supondo que existam essas propriedades
                    }
                    _financeReport.postValue(filteredReport)
                } else {
                    val response = apiService.getFinanceReport(startDate, endDate).execute()
                    if (response.isSuccessful) {
                        _financeReport.postValue(response.body())
                    } else {
                        _financeReport.postValue(null)
                        Log.e("ReportsViewModel", "Failed to fetch finance report: ${response.errorBody()}")
                    }
                }
            } catch (e: Exception) {
                Log.e("ReportsViewModel", "Error fetching finance report: ${e.message}", e)
                _financeReport.postValue(null)
            } finally {
                _loading.postValue(false)
            }
        }
    }


    fun searchWine(query: String) {
        val filteredWines = _soldWines.value?.filter {
            it.wineName.contains(query, ignoreCase = true)
        }
        _soldWines.postValue(filteredWines ?: emptyList())
    }

    fun searchStock(query: String) {
        val filteredList = _stockReport.value?.filter {
            it.name.contains(query, ignoreCase = true)
        }
        _filteredStockItems.postValue(filteredList ?: emptyList())
    }


}






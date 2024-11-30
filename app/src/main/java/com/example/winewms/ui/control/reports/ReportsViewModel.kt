package com.example.winewms.ui.control.reports


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.ui.control.reports.sales.BestSellingProduct
import com.example.winewms.ui.control.reports.sales.SalesComparison
import com.example.winewms.ui.control.reports.sales.SalesReportModel
import com.example.winewms.ui.control.reports.stock.StockItem

import kotlinx.coroutines.launch
import java.util.Calendar
class ReportsViewModel : ViewModel() {

    private val _salesComparison = MutableLiveData<SalesComparison?>()
    val salesComparison: LiveData<SalesComparison?> = _salesComparison

    private val _salesReport = MutableLiveData<SalesReportModel?>()
    val salesReport: LiveData<SalesReportModel?> = _salesReport

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _stockReport = MutableLiveData<List<StockItem>>()
    val stockReport: LiveData<List<StockItem>> = _stockReport


    // Toggle between mock data and real data
    private val isMockMode = true // Set to 'false' for real backend

    private val apiService: WineApiService by lazy {
        WineApi.retrofit.create(WineApiService::class.java)
    }

    fun fetchSalesComparison() {
        if (isMockMode) {
            // Mock data for testing
            val mockData = SalesComparison(
                currentMonthSales = 7500f, // Mock: sellings of the current month
                previousMonthSales = 5000f // Mock: sellings of the previous month
            )
            _salesComparison.postValue(mockData)
        } else {
            // Fetch real data from backend
            viewModelScope.launch {
                _loading.postValue(true)
                try {
                    val response = apiService.getSalesComparison().execute()
                    if (response.isSuccessful) {
                        _salesComparison.postValue(response.body())
                    } else {
                        _salesComparison.postValue(null)
                    }
                } catch (e: Exception) {
                    _salesComparison.postValue(null)
                } finally {
                    _loading.postValue(false)
                }
            }
        }
    }

    fun fetchSalesReportWithFilters(
        startDate: String,
        endDate: String,
        categories: List<String>,
        bestSellers: Boolean,

        ) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                if (isMockMode) {
                    // Mock data for testing
                    val mockData = SalesReportModel(
                        totalSales = 12345.67,
                        unitsSold = 456,
                        salesByCategory = categories.associateWith { 1000.0 }, // Mock sales by category
                        bestSellingProducts = if (bestSellers) {
                            listOf(
                                BestSellingProduct(
                                    productId = "123",
                                    productName = "Mock Wine A",
                                    totalUnitsSold = 200,
                                    totalRevenue = 2000.0
                                ),
                                BestSellingProduct(
                                    productId = "124",
                                    productName = "Mock Wine B",
                                    totalUnitsSold = 150,
                                    totalRevenue = 1500.0
                                )
                            )
                        } else {
                            emptyList()
                        }
                    )
                    // Post the mock data
                    _salesReport.postValue(mockData)
                } else {
                    // Call the API for real data
                    val response = apiService.getSalesReportWithFilters(
                        startDate, endDate, categories, bestSellers).execute()

                    if (response.isSuccessful) {
                        _salesReport.postValue(response.body())
                    } else {
                        _salesReport.postValue(null)
                    }
                }
            } catch (e: Exception) {
                _salesReport.postValue(null)
            } finally {
                _loading.postValue(false)
            }
        }
    }


    fun fetchStockReport(wineId: String? = null) {
        _loading.postValue(true)
        viewModelScope.launch {
            try {
                val response = apiService.getStockReport(wineId).execute()
                if (response.isSuccessful) {
                    _stockReport.postValue(response.body()) // Posta os dados recebidos
                } else {
                    _stockReport.postValue(emptyList()) // Posta uma lista vazia em caso de falha
                }
            } catch (e: Exception) {
                _stockReport.postValue(emptyList()) // Posta uma lista vazia em caso de exceção
            } finally {
                _loading.postValue(false)
            }
        }
    }


}


//Real data from backend
//fun fetchSalesComparison() {
//    viewModelScope.launch {
//        try {
//            val response = apiService.getSalesComparison().execute()
//            if (response.isSuccessful) {
//                val data = response.body()
//                if (data != null) {
//                    // Atualiza o LiveData com os valores reais do banco
//                    val salesComparison = SalesComparison(
//                        currentMonthSales = data.currentMonthSales,
//                        previousMonthSales = data.previousMonthSales
//                    )
//                    _salesComparison.postValue(salesComparison)
//                } else {
//                    _salesComparison.postValue(null)
//                }
//            } else {
//                _salesComparison.postValue(null)
//            }
//        } catch (e: Exception) {
//            _salesComparison.postValue(null)
//        }
//    }
//}
//}



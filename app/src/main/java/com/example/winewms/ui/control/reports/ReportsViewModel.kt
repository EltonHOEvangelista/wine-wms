package com.example.winewms.ui.control.reports


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

    private val _financeReport = MutableLiveData<FinanceReportModel?>()
    val financeReport: LiveData<FinanceReportModel?> = _financeReport

    private val _lowStockWines = MutableLiveData<List<StockItem>>()
    val lowStockWines: LiveData<List<StockItem>> = _lowStockWines


    private val apiService: WineApiService by lazy {
        WineApi.retrofit.create(WineApiService::class.java)
    }



    fun fetchSalesComparison() {
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

    fun fetchSalesReportWithFilters(
        startDate: String,
        endDate: String,
        categories: List<String>,
        bestSellers: Boolean,
    ) {
        _loading.postValue(true) // Show loading state

        viewModelScope.launch {
            try {
                val response = apiService.getSalesReportWithFilters(
                    startDate = startDate,
                    endDate = endDate,
                    categories = categories,
                    bestSellers = bestSellers
                ).execute()

                if (response.isSuccessful) {
                    _salesReport.postValue(response.body())
                } else {
                    _salesReport.postValue(null)
                }
            } catch (e: Exception) {
                _salesReport.postValue(null)
            } finally {
                _loading.postValue(false) // Hide loading state
            }
        }
    }

    fun fetchCurrentMonthFinanceReport() {
        viewModelScope.launch {
            try {
                val response = apiService.getFinanceReportForCurrentMonth().execute()
                if (response.isSuccessful) {
                    _financeReport.postValue(response.body())
                } else {
                    _financeReport.postValue(null)
                    Log.e("FinanceReportVM", "Error code: ${response.code()}, message: ${response.message()}")
                }
            } catch (e: Exception) {
                _financeReport.postValue(null)
                Log.e("FinanceReportVM", "Exception occurred: ${e.message}")
            }
        }
    }

    fun fetchFinanceReport(startDate: String, endDate: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getFinanceReport(startDate, endDate).execute()
                if (response.isSuccessful) {
                    _financeReport.postValue(response.body())
                } else {
                    _financeReport.postValue(null)
                    Log.e("FinanceReportVM", "Error code: ${response.code()}, message: ${response.message()}")
                }
            } catch (e: Exception) {
                _financeReport.postValue(null)
                Log.e("FinanceReportVM", "Exception occurred: ${e.message}")
            }
        }
    }



    fun fetchStockReport(wineId: String? = null) {
        _loading.postValue(true) // Mostrar estado de carregamento
        viewModelScope.launch {
            try {
                // Executa a chamada de forma síncrona
                val response = apiService.getStockReport(wineId).execute()

                if (response.isSuccessful) {
                    // Atualiza LiveData com os dados recebidos
                    _stockReport.postValue(response.body() ?: emptyList())
                } else {
                    // Atualiza com uma lista vazia em caso de erro
                    _stockReport.postValue(emptyList())
                    Log.e("StockReportVM", "Error code: ${response.code()}, message: ${response.message()}")
                }
            } catch (e: Exception) {
                // Atualiza com uma lista vazia em caso de exceção
                _stockReport.postValue(emptyList())
                Log.e("StockReportVM", "Exception occurred: ${e.message}")
            } finally {
                _loading.postValue(false) // Esconde estado de carregamento
            }
        }
    }

    fun fetchLowStockWines() {
        viewModelScope.launch {
            _loading.postValue(true)
            try {
                val response = apiService.getLowStockWines().execute()
                if (response.isSuccessful) {
                    _lowStockWines.postValue(response.body())
                } else {
                    _lowStockWines.postValue(emptyList())
                    Log.e("LowStockWines", "Error code: ${response.code()}, message: ${response.message()}")
                }
            } catch (e: Exception) {
                _lowStockWines.postValue(emptyList())
                Log.e("LowStockWines", "Exception occurred: ${e.message}")
            } finally {
                _loading.postValue(false)
            }
        }
    }





}





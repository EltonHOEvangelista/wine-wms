package com.example.winewms.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SalesResponseViewModel : ViewModel() {

    // MutableLiveData to store the list of invoices
    val _wineInvoiceList = MutableLiveData<List<WineInvoice>>()
    val wineInvoiceList: LiveData<List<WineInvoice>> get() = _wineInvoiceList

    // MutableLiveData to store the list of sales data
    val _listOfSales = MutableLiveData<List<CumulativeSales>>()
    val listOfSales: MutableLiveData<List<CumulativeSales>> get() = _listOfSales

    // Function to update the list
    fun setWineInvoiceList(list: List<WineInvoice>) {
        _wineInvoiceList.value = list
    }

    // Function to update the list
    fun setListOfSales(list: List<CumulativeSales>) {
        _listOfSales.value = list
    }

    // Function to clean or reset the Model View data
    fun clearWineInvoiceList() {
        _wineInvoiceList.value = emptyList()
    }

    // Function to clean or reset the Model View data
    fun clearSalesList() {
        _listOfSales.value = emptyList()
    }
}
package com.example.winewms.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.winewms.ui.account.AccountModel

class SalesResponseViewModel : ViewModel() {

    // MutableLiveData to store the list of wineModel object
    val _wineInvoiceList = MutableLiveData<List<WineInvoice>>()
    val wineInvoiceList: LiveData<List<WineInvoice>> get() = _wineInvoiceList

    // Function to update the list
    fun setWineInvoiceList(list: List<WineInvoice>) {
        _wineInvoiceList.value = list
    }

    // Function to clean or reset the AccountModel data
//    fun clearWineInvoiceList() {
//        _wineInvoiceList.value = null
//    }
}
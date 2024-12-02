package com.example.winewms.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class FinancialViewModel : ViewModel() {

    // MutableLiveData to store the list of financial data
    val _listOfCostRevenue = MutableLiveData<List<CumulativeCostRevenue>>()
    val listOfCostRevenue: MutableLiveData<List<CumulativeCostRevenue>> get() = _listOfCostRevenue

    // Function to update the list
    fun setListOfCostRevenue(list: List<CumulativeCostRevenue>) {
        _listOfCostRevenue.value = list
    }

    // Function to clean or reset the Financial View Model data
    fun clearFinancialViewModel() {
        _listOfCostRevenue.value = emptyList()
    }
}
package com.example.winewms.data.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SearchWineViewModel : ViewModel() {

    // MutableLiveData to store the list of wineModel object
    val _wineList = MutableLiveData<List<WineModel>>()
    val wineList: MutableLiveData<List<WineModel>> get() = _wineList

    // Function to update the list
    fun setWineList(list: List<WineModel>) {
        _wineList.value = list
    }

    // Function to clean or reset the SearchWineViewModel data
    fun clearSearchWineViewModel() {
        _wineList.value = emptyList()
    }
}
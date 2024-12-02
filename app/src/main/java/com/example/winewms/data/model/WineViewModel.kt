package com.example.winewms.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class WineViewModel : ViewModel() {

    // MutableLiveData to store the list of wineModel object
    val _wineList = MutableLiveData<List<WineModel>>()
    val wineList: LiveData<List<WineModel>> get() = _wineList

    // Function to update the list
    fun setWineList(list: List<WineModel>) {
        _wineList.value = list
    }

    // Function to add a wine to the list
    fun addWineOnList(wineModel: WineModel) {
        val currentList = _wineList.value.orEmpty()
        _wineList.value = currentList + wineModel
    }

    // Function to clean or reset the SearchWineViewModel data
    fun clearWineViewModel() {
        _wineList.value = emptyList()
    }
}
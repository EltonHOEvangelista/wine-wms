package com.example.winewms.ui.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {

    // MutableLiveData to hold the AccountModel object
    private val _account = MutableLiveData<AccountModel?>()
    val account: MutableLiveData<AccountModel?> get() = _account

    // Function to set or update the AccountModel data
    fun setAccount(account: AccountModel) {
        _account.value = account
    }

    // Function to clean or reset the AccountModel data
    fun clearAccount() {
        _account.value = null
    }
}


package com.example.winewms.data.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CartWineViewModel : ViewModel() {

    private val _cartItems = MutableLiveData<List<CartItemModel>>()
    val cartItems: LiveData<List<CartItemModel>> get() = _cartItems

    fun setCartItems(list: List<CartItemModel>) {
        _cartItems.value = list
    }

    fun updateCartItems(updatedList: List<CartItemModel>) {
        _cartItems.value = updatedList
    }
}
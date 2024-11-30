package com.example.winewms.data.json

import android.content.Context
import com.example.winewms.data.model.PurchaseDataWrapper
import com.example.winewms.data.model.WarehouseDataWrapper
import com.example.winewms.data.model.WarehouseModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoadStockFromJson {

    fun readJsonFile(context: Context, fileName: String): List<WarehouseModel> {

        var stockList: List<WarehouseModel> = emptyList()
        var jsonData: String? = null

        try {
            val inputStream = context.assets.open(fileName)
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonData = String(buffer, Charsets.UTF_8)
        }
        catch (e: Exception) {
            e.printStackTrace()
        }

        jsonData.let {
            val gson = Gson()
            val dataWrapperType = object : TypeToken<WarehouseDataWrapper>() {}.type
            val dataWrapper: WarehouseDataWrapper = gson.fromJson(it, dataWrapperType)
            stockList = dataWrapper.warehouses
        }

        return stockList
    }
}
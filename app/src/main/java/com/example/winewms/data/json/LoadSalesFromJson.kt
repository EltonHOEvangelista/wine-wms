package com.example.winewms.data.json

import android.content.Context
import com.example.winewms.data.model.SalesRequestDataWrapper
import com.example.winewms.data.model.SalesRequestModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoadSalesFromJson {

    fun readJsonFile(context: Context, fileName: String): List<SalesRequestModel> {

        var salesList: List<SalesRequestModel> = emptyList()
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
            val dataWrapperType = object : TypeToken<SalesRequestDataWrapper>() {}.type
            val dataWrapper: SalesRequestDataWrapper = gson.fromJson(it, dataWrapperType)
            salesList = dataWrapper.sales
        }

        return salesList
    }
}
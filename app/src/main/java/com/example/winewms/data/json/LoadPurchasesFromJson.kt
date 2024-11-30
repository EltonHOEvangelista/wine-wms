package com.example.winewms.data.json

import android.content.Context
import com.example.winewms.data.model.PurchaseDataWrapper
import com.example.winewms.data.model.PurchaseModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoadPurchasesFromJson {

    fun readJsonFile(context: Context, fileName: String): List<PurchaseModel> {

        var purchaseList: List<PurchaseModel> = emptyList()
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
            val dataWrapperType = object : TypeToken<PurchaseDataWrapper>() {}.type
            val dataWrapper: PurchaseDataWrapper = gson.fromJson(it, dataWrapperType)
            purchaseList = dataWrapper.purchases
        }

        return purchaseList
    }
}
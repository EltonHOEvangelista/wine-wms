package com.example.winewms.data.json

import android.content.Context
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.WineModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class LoadJson {

    fun readJsonFile(context: Context, fileName: String): List<WineModel> {

        var wineList: List<WineModel> = emptyList()
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
            val dataWrapperType = object : TypeToken<DataWrapper>() {}.type
            val dataWrapper: DataWrapper = gson.fromJson(it, dataWrapperType)
            wineList = dataWrapper.wines
        }

        return wineList
    }
}
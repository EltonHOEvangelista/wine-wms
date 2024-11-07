package com.example.winewms.data.json

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Locale

class LoadJson {

    fun readJsonFile(context: Context, fileName: String): List<Vehicle> {

        var vehicleList: List<Vehicle> = emptyList()
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
            vehicleList = dataWrapper.data
            vehicleList.map {
                val picId = context.resources.getIdentifier("v_" + it.Model.lowercase(Locale.getDefault()), "drawable", context.packageName)
                it.PicId = picId
            }
        }

        return vehicleList
    }
}
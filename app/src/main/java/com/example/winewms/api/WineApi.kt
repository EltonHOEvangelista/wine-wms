package com.example.winewms.api

import DateDeserializer
import com.google.gson.GsonBuilder
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Date

object WineApi {
    //use this on local development; 10.0.2.2it the localhost
    private const val BASE_URL = "http://10.0.2.2:8888/v1/api/"

    //use this in production
    //private const val BASE_URL = "http://52.0.58.207:8888/v1/api/"

    val retrofit: Retrofit by lazy {

        // Register the custom deserializer
        val gson = GsonBuilder()
            .registerTypeAdapter(Date::class.java, DateDeserializer())
            .create()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
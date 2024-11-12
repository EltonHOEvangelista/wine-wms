package com.example.winewms.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WineApi {
    private const val BASE_URL = "http://52.0.58.207:8888/v1/api/"
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
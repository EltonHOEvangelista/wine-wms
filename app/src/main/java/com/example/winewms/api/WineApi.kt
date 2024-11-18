package com.example.winewms.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WineApi {
    //use this on local development; 10.0.2.2it the localhost
    //private const val BASE_URL = "http://10.0.2.2:8888/v1/api/"

    //use this in production
    private const val BASE_URL = "http://52.0.58.207:8888/v1/api/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}
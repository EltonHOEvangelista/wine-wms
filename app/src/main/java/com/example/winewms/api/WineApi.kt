package com.example.winewms.api

import com.example.winewms.ui.account.AccountModel
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body

object WineApi {
    //use this on local development; 10.0.2.2it the localhost
    private const val BASE_URL = "http://192.168.1.206:8888/v1/api/"

    //use this in production
    //private const val BASE_URL = "http://52.0.58.207:8888/v1/api/"
    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val service: WineApiService = retrofit.create(WineApiService::class.java)

}
package com.example.winewms.api

import com.example.winewms.data.model.WineModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface WineApiService {

    //get all wines
    @GET("wines")
    fun getAllWines(): Call<List<WineModel>>

    //get a single wine by ID
    @GET("wines/{id}")
    fun getAllWines(@Path("id") wineId: String): Call<WineModel>

    //create a new wine
    @POST("wines")
    fun createWine(@Body wineModel: WineModel): Call<String>

    //update an existing wine by ID
    @PUT("wines/{id}")
    fun updateWine(@Path("id") wineId: String, @Body wineModel: WineModel): Call<String>

    //delete an existing wine by ID
    @DELETE("wines/{id}")
    fun deleteWine(@Path("id") wineId: String): Call<String>

    //query wines by partial name
    @GET("wines/search")
    fun searchWines(@Query("filter") filter: String): Call<List<WineModel>>

    //filter wines by type
    @GET("wines/filter")
    fun filterWinesByType(@Query("type") wineType: String): Call<List<WineModel>>

    //remove current wines and create new ones (initial list)
    @POST("wines/all")
    fun createInitialWines(@Body wineList: List<WineModel>): Call<String>
}
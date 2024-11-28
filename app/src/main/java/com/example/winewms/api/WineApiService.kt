package com.example.winewms.api

import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.PurchaseModel
import com.example.winewms.data.model.ResponseModel
import com.example.winewms.data.model.SalesDataWrapper
import com.example.winewms.data.model.SalesModel
import com.example.winewms.data.model.WarehouseModel
import com.example.winewms.data.model.WineModel
import com.example.winewms.ui.account.AccountDataWrapper
import com.example.winewms.ui.account.AccountModel
import com.example.winewms.ui.account.signin.SigninModel
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap

interface WineApiService {

    //---------------------------------------------------------------------------
    // Account endpoints

    // Signup (register a new account)
    @POST("account/signup")
    fun signup(@Body accountModel: AccountModel): Call<AccountDataWrapper>

    // Signin (login an existing account)
    @POST("account/signin")
    fun signin(@Body signinModel: SigninModel): Call<AccountDataWrapper>

    // Delete an account
    @DELETE("account/delete")
    fun deleteAccount(@Query("email") email: String, @Query("password") password: String): Call<AccountDataWrapper>

    //---------------------------------------------------------------------------
    // Wine endpoints

    // Get all wines with pagination (returns DataWrapper with wines list and pagination details)
    @GET("wines")
    fun getWines(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 10,
        @QueryMap filters: Map<String, String>? = emptyMap()
    ): Call<DataWrapper>

    //get a single wine by ID
    @GET("wines/{id}")
    fun getWineById(@Path("id") wineId: String): Call<WineModel>

    //create a new wine
    //return wine id from backend
    @POST("wines")
    fun createWine(@Body wineModel: WineModel): Call<ResponseModel>

    //update an existing wine by ID
    @PUT("wines/{id}")
    fun updateWine(@Path("id") wineId: String, @Body wineModel: WineModel): Call<String>

    //delete an existing wine by ID
    @DELETE("wines/{id}")
    fun deleteWine(@Path("id") wineId: String): Call<String>

    //remove current wines and create new ones (initial list)
    @POST("wines/all")
    fun createInitialWines(@Body wineList: List<WineModel>): Call<ResponseModel>

    @POST("wines/bulk")
    fun getBulkWines(@Body wineIds: List<String>): Call<List<WineModel>>

    //---------------------------------------------------------------------------
    // Purchases endpoints

    @POST("purchase/all")
    fun create_initial_purchase(@Body purchaseList: List<PurchaseModel>): Call<ResponseModel>

    @POST("purchase")
    fun placePurchaseOrder(@Body purchaseModel: PurchaseModel): Call<ResponseModel>

    //---------------------------------------------------------------------------
    // Sales endpoints

    @POST("sales")
    fun placeSalesOrder(@Body salesModel: SalesModel): Call<SalesDataWrapper>

    @GET("sales/customer/{id}")
    fun getSalesOrdersByCustomerId(@Path("id") wineId: String): Call<SalesDataWrapper>

    //---------------------------------------------------------------------------
    // Warehouse/Stock endpoints

    @POST("warehouse/all")
    fun create_initial_stock(@Body warehouseList: List<WarehouseModel>): Call<ResponseModel>

    @POST("warehouse")
    fun updateStock(@Body warehouseModel: WarehouseModel): Call<ResponseModel>
}
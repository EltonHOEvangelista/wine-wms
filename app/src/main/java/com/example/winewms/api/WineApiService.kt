package com.example.winewms.api

import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.PurchaseModel
import com.example.winewms.data.model.ReplenishStockRequest
import com.example.winewms.data.model.ReplenishStockResponse
import com.example.winewms.data.model.ResponseModel
import com.example.winewms.data.model.SalesDataWrapper
import com.example.winewms.data.model.SalesModel
import com.example.winewms.data.model.WarehouseModel
import com.example.winewms.data.model.WineModel
import com.example.winewms.ui.account.AccountDataWrapper
import com.example.winewms.ui.account.AccountModel
import com.example.winewms.ui.account.signin.SigninModel
import com.example.winewms.ui.control.reports.ReportModel
import com.example.winewms.ui.control.reports.sales.SalesComparison
import com.example.winewms.ui.control.reports.sales.SalesReportModel
import com.example.winewms.ui.control.reports.stock.StockItem
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
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
    @PATCH("wines/{id}")
    fun updateWine(@Path("id") wineId: String, @Body wineModel: WineModel): Call<String>

    //delete an existing wine by ID
    @DELETE("wines/{id}")
    fun deleteWine(@Path("id") wineId: String): Call<ResponseModel>

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
    fun getOrdersByCustomerId(@Path("id") wineId: String): Call<SalesDataWrapper>

    //---------------------------------------------------------------------------
    // Warehouse/Stock endpoints

    @GET("warehouses")
    fun getWarehouses(): Call<List<WarehouseModel>>

    @POST("warehouse/all")
    fun create_initial_stock(@Body warehouseList: List<WarehouseModel>): Call<ResponseModel>

    @POST("warehouse")
    fun updateStock(@Body warehouseModel: WarehouseModel): Call<ResponseModel>

    //---------------------------------------------------------------------------
    // Reports endpoints

     //Get Sales Report
     @GET("sales/report")
     fun getSalesReportWithFilters(
         @Query("start_date") startDate: String,
         @Query("end_date") endDate: String,
         @Query("categories") categories: List<String>,
         @Query("best_sellers") bestSellers: Boolean,
     ): Call<SalesReportModel>

    @GET("sales/comparison")
    fun getSalesComparison(): Call<SalesComparison>

    //Get Stock Report
    @GET("stock/report")
    fun getStockReport(
        @Query("wineId") wineId: String? = null
    ): Call<List<StockItem>>

    @POST("wines/{id}/replenish")
    fun replenishStock(
        @Path("id") id: String,
        @Body replenishRequest: ReplenishStockRequest
    ): Call<ReplenishStockResponse>

}
package com.example.winewms

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.json.LoadPurchasesFromJson
import com.example.winewms.data.json.LoadSalesFromJson
import com.example.winewms.data.json.LoadStockFromJson
import com.example.winewms.data.json.LoadWinesFromJson
import com.example.winewms.data.model.PurchaseModel
import com.example.winewms.data.model.ResponseModel
import com.example.winewms.data.model.SalesRequestModel
import com.example.winewms.data.model.WarehouseModel
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.sql.DatabaseHelper
import com.example.winewms.databinding.ActivityMainBinding
import com.example.winewms.ui.account.AccountViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    lateinit var wineList: List<WineModel>
    lateinit var purchaseList: List<PurchaseModel>
    lateinit var stockList: List<WarehouseModel>
    lateinit var salesList: List<SalesRequestModel>

    //Variable to manage bottom navigation view
    lateinit var navView: BottomNavigationView
    private val accountViewModel: AccountViewModel by viewModels()

    //Instantiate Wine Api
    var wineApi = WineApi.retrofit.create(WineApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Bottom Navigation View Setup
        setBottomNavigationView()

        //Load initial data from json file into MongoDB
        //loadInitialData()

        //Start opened account session
        startOpenedAccountSession()
    }

    private fun loadInitialData() {

        //Upload initial wines to serve
        //loadInitialWinesOnServer()

        //Upload initial purchases to serve
        //loadInitialPurchasesOnServer()

        //Upload initial stock to serve
        //loadInitialStockOnServer()

        //Upload initial sales to serve
        //loadInitialSalesOnServer()
    }

    private fun loadInitialSalesOnServer() {

        //Load purchases from json file
        val dataFile = LoadSalesFromJson()
        salesList = dataFile.readJsonFile(this,"sales_list.json")!!

        //Making an API call to push initial data to the backend
        val apiCall = wineApi.create_initial_sales(salesList)

        //Enqueueing the API call for asynchronous execution
        apiCall.enqueue(object : Callback<ResponseModel> { // <Unit>

            // Handling the failure scenario
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                //Displaying a toast message for user feedback
                Toast.makeText(baseContext, "Failed to load initial data into MongoDB.", Toast.LENGTH_SHORT).show()
                // Logging the error details for debugging
                Log.d("API Service", "Error loading initial data: ${t.message}")
            }

            // Handling the response
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    //Successfully pushed data to the backend
                    Toast.makeText(baseContext, "Initial data loaded successfully.", Toast.LENGTH_SHORT).show()
                    Log.d("API Service", "Initial data loaded successfully.")
                } else {
                    //Handling unsuccessful response
                    Log.e("API Service", "Failed to load initial data. Error: ${response.errorBody()?.string()}")
                    Toast.makeText(baseContext, "Failed to load initial data.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadInitialPurchasesOnServer() {

        //Load purchases from json file
        val dataFile = LoadPurchasesFromJson()
        purchaseList = dataFile.readJsonFile(this,"purchase_list.json")!!

        //Making an API call to push initial data to the backend
        val apiCall = wineApi.create_initial_purchase(purchaseList)

        //Enqueueing the API call for asynchronous execution
        apiCall.enqueue(object : Callback<ResponseModel> { // <Unit>

            // Handling the failure scenario
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                //Displaying a toast message for user feedback
                Toast.makeText(baseContext, "Failed to load initial data into MongoDB.", Toast.LENGTH_SHORT).show()
                // Logging the error details for debugging
                Log.d("API Service", "Error loading initial data: ${t.message}")
            }

            // Handling the response
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    //Successfully pushed data to the backend
                    Toast.makeText(baseContext, "Initial data loaded successfully.", Toast.LENGTH_SHORT).show()
                    Log.d("API Service", "Initial data loaded successfully.")
                } else {
                    //Handling unsuccessful response
                    Log.e("API Service", "Failed to load initial data. Error: ${response.errorBody()?.string()}")
                    Toast.makeText(baseContext, "Failed to load initial data.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun loadInitialStockOnServer() {

        //Load stock from json file
        val dataFile = LoadStockFromJson()
        stockList = dataFile.readJsonFile(this,"warehouse_list.json")!!

        //Making an API call to push initial data to the backend
        val apiCall = wineApi.create_initial_stock(stockList)

        //Enqueueing the API call for asynchronous execution
        apiCall.enqueue(object : Callback<ResponseModel> { // <Unit>

            // Handling the failure scenario
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                //Displaying a toast message for user feedback
                Toast.makeText(baseContext, "Failed to load initial data into MongoDB.", Toast.LENGTH_SHORT).show()
                // Logging the error details for debugging
                Log.d("API Service", "Error loading initial data: ${t.message}")
            }

            // Handling the response
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    //Successfully pushed data to the backend
                    Toast.makeText(baseContext, "Initial data loaded successfully.", Toast.LENGTH_SHORT).show()
                    Log.d("API Service", "Initial data loaded successfully.")
                } else {
                    //Handling unsuccessful response
                    Log.e("API Service", "Failed to load initial data. Error: ${response.errorBody()?.string()}")
                    Toast.makeText(baseContext, "Failed to load initial data.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    //Function to load initial data from json file to MongoDB
    private fun loadInitialWinesOnServer() {

        //Load wines from json file
        val dataFile = LoadWinesFromJson()
        wineList = dataFile.readJsonFile(this,"wine_list.json")!!

        //Making an API call to push initial data (wineList) to the backend
        val apiCall = wineApi.createInitialWines(wineList)

        //Enqueueing the API call for asynchronous execution
        apiCall.enqueue(object : Callback<ResponseModel> { // <Unit>

            // Handling the failure scenario
            override fun onFailure(call: Call<ResponseModel>, t: Throwable) {
                //Displaying a toast message for user feedback
                Toast.makeText(baseContext, "Failed to load initial data into MongoDB.", Toast.LENGTH_SHORT).show()
                // Logging the error details for debugging
                Log.d("API Service", "Error loading initial data: ${t.message}")
            }

            // Handling the response
            override fun onResponse(call: Call<ResponseModel>, response: Response<ResponseModel>) {
                if (response.isSuccessful) {
                    //Successfully pushed data to the backend
                    Toast.makeText(baseContext, "Initial data loaded successfully.", Toast.LENGTH_SHORT).show()
                    Log.d("API Service", "Initial data loaded successfully.")
                } else {
                    //Handling unsuccessful response
                    Log.e("API Service", "Failed to load initial data. Error: ${response.errorBody()?.string()}")
                    Toast.makeText(baseContext, "Failed to load initial data.", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun startOpenedAccountSession() {

        val dbHelper by lazy {
            DatabaseHelper(this)
        }

        val accountModel = dbHelper.getActiveSessionAccount()
        if (accountModel != null) {
            // Store fetched data in the ViewModel
            accountViewModel.setAccount(accountModel)

            //Activate UI admin features. Type = 1 (administrator)
            if (accountModel.type == 1) {
                navView.menu.findItem(R.id.navigation_control)?.isVisible = true
                navView.menu.findItem(R.id.navigation_control)?.isEnabled = true
            }

            Toast.makeText(this, "Welcome to Wine Warehouse, ${accountModel.firstName}!", Toast.LENGTH_SHORT).show()
        }
        dbHelper.close()
    }

    private fun setBottomNavigationView() {
        navView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        //Action Bar Setup
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.action_bar)
    }
}
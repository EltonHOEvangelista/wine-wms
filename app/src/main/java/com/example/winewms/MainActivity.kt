package com.example.winewms

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.winewms.api.WineApi
import com.example.winewms.api.WineApiService
import com.example.winewms.data.json.LoadJson
import com.example.winewms.data.model.DataWrapper
import com.example.winewms.data.model.SearchWineViewModel
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.data.sql.DatabaseHelper
import com.example.winewms.databinding.ActivityMainBinding
import com.example.winewms.ui.account.AccountModel
import com.example.winewms.ui.account.AccountViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //wine list variable
    lateinit var wineList: List<WineModel>

    //Variable to manage bottom navigation view
    lateinit var navView: BottomNavigationView

    //variable used to transfer objects among activities and fragments
    val wineViewModel: WineViewModel by viewModels()
    private val searchWineViewModel: SearchWineViewModel by viewModels()
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
        //loadInitialDataIntoMongoDB()

        //Fetch data from backend using Wine API
        fetchDataFromBackend()

        //Starts the app by
        fetchAllWines()

        //Start opened account session
        startOpenedAccountSession()
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

    //Function to load initial data from json file to MongoDB
    private fun loadInitialDataIntoMongoDB() {

        //Load wines from json file
        loadWinesFromJsonFile()

        //Making an API call to push initial data (wineList) to the backend
        val apiCall = wineApi.createInitialWines(wineList)

        //Enqueueing the API call for asynchronous execution
        apiCall.enqueue(object : Callback<String> { // <Unit>

            // Handling the failure scenario
            override fun onFailure(call: Call<String>, t: Throwable) {
                //Displaying a toast message for user feedback
                Toast.makeText(baseContext, "Failed to load initial data into MongoDB.", Toast.LENGTH_SHORT).show()
                // Logging the error details for debugging
                Log.d("API Service", "Error loading initial data: ${t.message}")
            }

            // Handling the response
            override fun onResponse(call: Call<String>, response: Response<String>) {
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

    //temporary function to load mock data from json file
    private fun loadWinesFromJsonFile() {

        val dataFile = LoadJson()
        wineList = dataFile.readJsonFile(this,"wine_list.json")!!
    }

    //Function to fetch data from backend using Wine API
    private fun fetchDataFromBackend() {

        // Set up filter for discounts great of equal to 15%
        val filters = mutableMapOf<String, String>()
        filters["discount"] = "0.15"
        // Fetch filtered data from api
        val apiCall = wineApi.getAllWines(filters = filters)

        //Asynchronous call to fetch data from Wine's Api
        apiCall.enqueue(object: Callback<DataWrapper> {

            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(baseContext, "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.e("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    //Successfully fetched data
                    val dataWrapper = response.body()
                    if (dataWrapper != null) {
                        wineList = dataWrapper.wines

                        //Loading Wine View Model. It's required to share the wineModel object among fragments
                        wineViewModel.setWineList(wineList)
                    }
                }
                else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }

    private fun fetchAllWines() {
        val wineApi = WineApi.retrofit.create(WineApiService::class.java)
        val apiCall = wineApi.getAllWines()

        apiCall.enqueue(object : Callback<DataWrapper> {
            override fun onFailure(call: Call<DataWrapper>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to fetch data.", Toast.LENGTH_SHORT).show()
            }
            override fun onResponse(call: Call<DataWrapper>, response: Response<DataWrapper>) {
                if (response.isSuccessful) {
                    response.body()?.wines?.let {
                        searchWineViewModel.setWineList(it)  // Store fetched data in the ViewModel
                    }
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
}
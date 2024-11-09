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
import com.example.winewms.data.json.LoadJson
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.ActivityMainBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //wine list variable
    lateinit var wineList: List<WineModel>

    //variable used to transfer objects among activities and fragments
    val wineViewModel: WineViewModel by viewModels()

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
    }

    private fun setBottomNavigationView() {
        val navView: BottomNavigationView = binding.navView
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)

        //Action Bar Setup
        supportActionBar?.displayOptions = ActionBar.DISPLAY_SHOW_CUSTOM
        supportActionBar?.setCustomView(R.layout.action_bar)
    }

    //function to load Wine View Model. It's required to share the wineModel object among fragments
    private fun loadWineViewModel() {
        wineViewModel.setWineList(wineList)
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

        //Loading Wine View Model. It's required to share the wineModel object among fragments
        //loadWineViewModel()
    }

    //Function to fetch data from backend using Wine API
    private fun fetchDataFromBackend() {

        val apiCall = wineApi.getAllWines()

        //Asynchronous call to fetch data from Wine's Api
        apiCall.enqueue(object: Callback<List<WineModel>> {

            override fun onFailure(call: Call<List<WineModel>>, t: Throwable) {
                Toast.makeText(baseContext, "Failed to fetch data.", Toast.LENGTH_SHORT).show()
                Log.d("API Service Failure", t.message.toString())
            }

            override fun onResponse(call: Call<List<WineModel>>, response: Response<List<WineModel>>) {
                if (response.isSuccessful) {
                    //Successfully fetched data
                    wineList = response.body()!!

                    //Loading Wine View Model. It's required to share the wineModel object among fragments
                    loadWineViewModel()
                }
                else {
                    Log.e("API Service Response", "Failed to fetch data. Error: ${response.errorBody()?.string()}")
                }
            }
        })
    }
}
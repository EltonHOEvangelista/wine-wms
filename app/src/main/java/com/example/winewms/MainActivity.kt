package com.example.winewms

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.winewms.data.json.LoadJson
import com.example.winewms.data.model.WineModel
import com.example.winewms.data.model.WineViewModel
import com.example.winewms.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //wine list variable
    lateinit var wineList: List<WineModel>

    //variable used to transfer objects among activities and fragments
    val wineViewModel: WineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Bottom Navigation View Setup
        setBottomNavigationView()

        //Loading mock data from json file
        loadMockData()
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

    //temporary function to load mock data from json file
    private fun loadMockData() {

        val dataFile = LoadJson()
        wineList = dataFile.readJsonFile(this,"wine_list.json")!!

        //Loading Wine View Model. It's required to share the wineModel object among fragments
        loadWineViewModel()
    }

    //function to load Wine View Model. It's required to share the wineModel object among fragments
    private fun loadWineViewModel() {
        wineViewModel.setWineList(wineList)
    }
}
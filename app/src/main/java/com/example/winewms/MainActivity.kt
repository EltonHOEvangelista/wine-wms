package com.example.winewms

import android.os.Bundle
import androidx.appcompat.app.ActionBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.winewms.databinding.ActivityMainBinding
import com.example.winewms.ui.account.AccountFragment
import com.example.winewms.ui.account.signin.SigninFragment
import com.example.winewms.ui.cart.CartFragment
import com.example.winewms.ui.control.ControlFragment
import com.example.winewms.ui.dashboard.DashboardFragment
import com.example.winewms.ui.home.HomeFragment
import com.example.winewms.ui.wishlist.WishListFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    //variable to handle fragment display
    lateinit var homeFragment: Fragment
    lateinit var wishlistFragment: Fragment
    lateinit var cartFragment: Fragment

    lateinit var controlFragment: Fragment
    lateinit var dashboardFragment: Fragment

    lateinit var accountFragment: Fragment
    lateinit var signinFragment: Fragment //sub-menu

    var activeFragment: Fragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize fragments
        //initializeFragments()

        //Bottom Navigation View Setup
        setBottomNavigationView()
    }

    private fun initializeFragments() {

        homeFragment = HomeFragment()
        wishlistFragment = WishListFragment()
        cartFragment = CartFragment()
        controlFragment = ControlFragment()
        dashboardFragment = DashboardFragment()
        accountFragment = AccountFragment()
        signinFragment = SigninFragment()

        // Add fragments to FragmentManager and hide them initially
        supportFragmentManager.beginTransaction().apply {
            add(R.id.fragmentContainerView, homeFragment, "Home").hide(homeFragment)
            add(R.id.fragmentContainerView, wishlistFragment, "Wishlist").hide(wishlistFragment)
            add(R.id.fragmentContainerView, cartFragment, "Cart").hide(cartFragment)
            add(R.id.fragmentContainerView, controlFragment, "Control").hide(controlFragment)
            add(R.id.fragmentContainerView, dashboardFragment, "Dashboard").hide(dashboardFragment)
            add(R.id.fragmentContainerView, accountFragment, "Account").hide(accountFragment)
            add(R.id.fragmentContainerView, signinFragment, "Signin").hide(signinFragment)
        }.commit()

        // Show initial fragment
        activeFragment = homeFragment
        supportFragmentManager.beginTransaction().show(homeFragment).commit()

        setupBottomNavigation()
    }

    private fun setupBottomNavigation() {

        val bottomNavigation = findViewById<BottomNavigationView>(R.id.nav_view)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> switchFragment(homeFragment)
                R.id.navigation_wishlist -> switchFragment(wishlistFragment)
                R.id.navigation_cart -> switchFragment(cartFragment)
                R.id.navigation_control -> switchFragment(controlFragment)
                R.id.navigation_dashboard -> switchFragment(dashboardFragment)
                R.id.navigation_account -> switchFragment(accountFragment)
                R.id.navigation_signin -> switchFragment(signinFragment)
                else -> false
            }
        }
    }

    private fun switchFragment(targetFragment: Fragment): Boolean {
        if (activeFragment == targetFragment) return true
        supportFragmentManager.beginTransaction().apply {
            hide(activeFragment!!)
            show(targetFragment)
        }.commit()
        activeFragment = targetFragment
        return true
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
}
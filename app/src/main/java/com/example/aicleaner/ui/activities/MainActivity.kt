package com.example.aicleaner.ui.activities

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.aicleaner.R
import com.example.aicleaner.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() ,NavController.OnDestinationChangedListener{

    private var navController: NavController? = null
    private var bindingMain:ActivityMainBinding?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bindingMain=DataBindingUtil.setContentView(this,R.layout.activity_main)
        setSupportActionBar(bindingMain?.toolBar)

        setUpNavigationController()
    }

    //Setup Navigation Graph....
    private fun setUpNavigationController() {
        val navHostFragment: NavHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        val appBarConfigurationList = AppBarConfiguration(
            setOf(
                R.id.mainFragment,
                R.id.appManager
            )
        )
        setupActionBarWithNavController(navController!!,appBarConfigurationList)
        navController!!.addOnDestinationChangedListener(this)
    }
    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {

    }
}

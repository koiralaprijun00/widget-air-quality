package com.example.nepalweatherwidget

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.example.nepalweatherwidget.core.network.NetworkMonitor
import com.example.nepalweatherwidget.presentation.ui.dashboard.DashboardFragment
import com.example.nepalweatherwidget.presentation.ui.map.MapFragment
import com.example.nepalweatherwidget.presentation.ui.locations.LocationsFragment
import com.example.nepalweatherwidget.presentation.ui.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchFragment.SearchListener {
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor
    
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var fabSearch: FloatingActionButton
    private var networkSnackbar: Snackbar? = null
    
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Fine location access granted
                onLocationPermissionGranted()
            }
            permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only coarse location access granted
                onLocationPermissionGranted()
            }
            else -> {
                // No location access granted
                showLocationPermissionRationale()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeViews()
        setupBottomNavigation()
        setupNetworkObserver()
        checkLocationPermission()
        
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_dashboard
        }
    }
    
    private fun initializeViews() {
        bottomNav = findViewById(R.id.bottom_nav)
        fabSearch = findViewById(R.id.fab_search)
        
        fabSearch.setOnClickListener {
            navigateToSearch()
        }
    }
    
    private fun setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    loadFragment(DashboardFragment())
                    fabSearch.show()
                    true
                }
                R.id.nav_map -> {
                    loadFragment(MapFragment())
                    fabSearch.hide()
                    true
                }
                R.id.nav_locations -> {
                    loadFragment(LocationsFragment())
                    fabSearch.hide()
                    true
                }
                else -> false
            }
        }
    }
    
    private fun setupNetworkObserver() {
        lifecycleScope.launch {
            networkMonitor.isOnline.collectLatest { isOnline ->
                if (!isOnline) {
                    showNetworkError()
                } else {
                    dismissNetworkError()
                }
            }
        }
    }
    
    private fun checkLocationPermission() {
        when {
            hasLocationPermission() -> {
                // Permission already granted
                onLocationPermissionGranted()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showLocationPermissionRationale()
            }
            else -> {
                requestLocationPermission()
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    private fun requestLocationPermission() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }
    
    private fun showLocationPermissionRationale() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Location Permission Required")
            .setMessage("This app needs location permission to show weather data for your current location.")
            .setPositiveButton("Grant") { _, _ ->
                requestLocationPermission()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
                // Use default location
                onLocationPermissionDenied()
            }
            .show()
    }
    
    private fun onLocationPermissionGranted() {
        // Notify current fragment about permission granted
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is DashboardFragment) {
            currentFragment.onLocationPermissionGranted()
        }
    }
    
    private fun onLocationPermissionDenied() {
        // Use default location (Kathmandu)
        Snackbar.make(
            findViewById(android.R.id.content),
            "Using default location: Kathmandu",
            Snackbar.LENGTH_LONG
        ).show()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.fade_out
            )
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
    
    private fun navigateToSearch() {
        loadFragment(SearchFragment.newInstance(this))
        fabSearch.hide()
    }

    override fun onLocationSearched(location: String) {
        val dashboardFragment = DashboardFragment.newInstance(location)
        loadFragment(dashboardFragment)
        fabSearch.show()
        bottomNav.selectedItemId = R.id.nav_dashboard
    }
    
    private fun showNetworkError() {
        networkSnackbar?.dismiss()
        networkSnackbar = Snackbar.make(
            findViewById(android.R.id.content),
            "No internet connection. Some features may be limited.",
            Snackbar.LENGTH_INDEFINITE
        ).apply {
            setAction("Settings") {
                // Open network settings
                startActivity(Intent(android.provider.Settings.ACTION_WIRELESS_SETTINGS))
            }
            show()
        }
    }
    
    private fun dismissNetworkError() {
        networkSnackbar?.dismiss()
        networkSnackbar = null
    }
    
    override fun onDestroy() {
        networkSnackbar?.dismiss()
        super.onDestroy()
    }
} 
package com.example.nepalweatherwidget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.example.nepalweatherwidget.presentation.ui.dashboard.DashboardFragment
import com.example.nepalweatherwidget.presentation.ui.map.MapFragment
import com.example.nepalweatherwidget.presentation.ui.locations.LocationsFragment
import com.example.nepalweatherwidget.presentation.ui.search.SearchFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), SearchFragment.SearchListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        val fabSearch = findViewById<FloatingActionButton>(R.id.fab_search)

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

        // Show DashboardFragment by default only on first creation
        if (savedInstanceState == null) {
            bottomNav.selectedItemId = R.id.nav_dashboard
        }

        fabSearch.setOnClickListener {
            // Navigate to SearchFragment
            loadFragment(SearchFragment.newInstance(this))
            fabSearch.hide()
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    override fun onLocationSearched(location: String) {
        // After search, return to dashboard and update location
        val dashboardFragment = DashboardFragment.newInstance(location)
        loadFragment(dashboardFragment)
        findViewById<FloatingActionButton>(R.id.fab_search).show()
    }
} 
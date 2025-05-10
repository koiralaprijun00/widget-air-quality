package com.example.nepalweatherwidget.ui

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.widget.TraditionalWeatherWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.content.SharedPreferences

@AndroidEntryPoint
class WidgetConfigActivity : AppCompatActivity() {
    
    @Inject
    lateinit var preferences: SharedPreferences
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_widget_config)
        
        // Get widget ID from intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setupUI()
    }
    
    private fun setupUI() {
        // Add location selection
        val locationSpinner = findViewById<Spinner>(R.id.locationSpinner)
        val locations = listOf("Kathmandu", "Pokhara", "Lalitpur", "Bhaktapur")
        
        locationSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, locations)
        
        findViewById<Button>(R.id.addWidgetButton).setOnClickListener {
            val selectedLocation = locationSpinner.selectedItem as String
            
            // Save location preference
            preferences.edit()
                .putString("widget_location_$appWidgetId", selectedLocation)
                .apply()
            
            // Update widget
            val appWidgetManager = AppWidgetManager.getInstance(this)
            TraditionalWeatherWidgetProvider.updateWidgetNow(this, appWidgetManager, appWidgetId)
            
            // Return result
            val resultValue = Intent().apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            setResult(RESULT_OK, resultValue)
            finish()
        }
    }
} 
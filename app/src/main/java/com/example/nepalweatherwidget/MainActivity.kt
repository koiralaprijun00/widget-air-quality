package com.example.nepalweatherwidget

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        // Start the worker for widget updates
        WeatherUpdateWorker.startPeriodicUpdate(applicationContext)
    }
} 
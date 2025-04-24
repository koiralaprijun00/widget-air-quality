package com.example.nepalweatherwidget

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "MainActivity created")
        
        // Only set content view if this is a normal app launch
        if (intent?.action == null) {
            setContentView(R.layout.activity_main)
        }
    }
} 
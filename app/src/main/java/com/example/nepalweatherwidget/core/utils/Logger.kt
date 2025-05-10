package com.example.nepalweatherwidget.core.util

import android.util.Log
import com.example.nepalweatherwidget.BuildConfig

object Logger {
    private const val TAG = "WeatherApp"
    
    fun d(message: String, tag: String = TAG) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }
    
    fun e(message: String, throwable: Throwable? = null, tag: String = TAG) {
        Log.e(tag, message, throwable)
        // Add crash reporting here (e.g., Firebase Crashlytics)
    }
    
    fun w(message: String, tag: String = TAG) {
        Log.w(tag, message)
    }
} 
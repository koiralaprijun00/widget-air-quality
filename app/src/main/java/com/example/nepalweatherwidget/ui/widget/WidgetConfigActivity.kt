package com.example.nepalweatherwidget.ui.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WidgetConfigActivity : ComponentActivity() {
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED in case the user backs out
        setResult(RESULT_CANCELED)
        
        // Find the widget id from the intent
        intent?.extras?.let {
            appWidgetId = it.getInt(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID
            )
        }
        
        // If they gave us an intent without a widget id, just bail
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            // TODO: Add your configuration UI here
        }
    }
    
    private fun createWidget() {
        // TODO: Save widget configuration
        
        // Push widget update to surface with newly set configuration
        val appWidgetManager = AppWidgetManager.getInstance(this)
        
        // Make sure we pass back the original appWidgetId
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(RESULT_OK, resultValue)
        finish()
    }
}
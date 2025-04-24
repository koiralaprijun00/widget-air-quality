package com.example.nepalweatherwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.nepalweatherwidget.R

class TraditionalWeatherWidgetProvider : AppWidgetProvider() {
    private val TAG = "TraditionalWidget"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called with ${appWidgetIds.size} widgets")
        
        // Update each widget
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }
    
    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        try {
            Log.d(TAG, "Updating widget ID: $widgetId")
            
            // Create a RemoteViews for the widget layout
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            // Set static data for now
            views.setTextViewText(R.id.location, "Kathmandu, Nepal")
            views.setTextViewText(R.id.air_quality, "AQI: 85 (Moderate)")
            views.setTextViewText(R.id.air_quality_description, "Partly Cloudy, 25°C")
            views.setTextViewText(R.id.pm25, "PM2.5: 15.2 μg/m³")
            views.setTextViewText(R.id.pm10, "PM10: 32.7 μg/m³")
            views.setTextViewText(R.id.last_updated, "Updated: Apr 24, 10:30 AM")
            
            // Update the widget
            appWidgetManager.updateAppWidget(widgetId, views)
            Log.d(TAG, "Widget ID $widgetId updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget ID $widgetId", e)
        }
    }
    
    override fun onEnabled(context: Context) {
        Log.d(TAG, "Widget enabled for the first time")
    }
    
    override fun onDisabled(context: Context) {
        Log.d(TAG, "All widget instances removed")
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        super.onReceive(context, intent)
    }
} 
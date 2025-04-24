package com.example.nepalweatherwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.RemoteViews
import com.example.nepalweatherwidget.MainActivity
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
            
            // Get weather data
            val weatherData = GetWeatherUseCase.getMockWeatherData()
            Log.d(TAG, "Got weather data: $weatherData")
            
            // Create a RemoteViews for the widget layout
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            // Set the weather data
            views.setTextViewText(R.id.location, weatherData.location)
            views.setTextViewText(R.id.weather_description, weatherData.description)
            views.setTextViewText(R.id.temperature, "${weatherData.temperature}°C")
            
            // Set environmental data
            views.setTextViewText(R.id.humidity, "${weatherData.humidity}%")
            views.setTextViewText(R.id.wind_speed, "${weatherData.windSpeed} m/s")
            
            // Set AQI data
            views.setTextViewText(R.id.aqi_value, "85")
            views.setTextViewText(R.id.aqi_circle, "85")
            
            // Set the last updated time
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val currentTime = sdf.format(Date(weatherData.timestamp))
            views.setTextViewText(R.id.last_updated, "Last updated: $currentTime")
            
            // Set up click intent for the entire widget
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
            
            // Update the widget
            appWidgetManager.updateAppWidget(widgetId, views)
            Log.d(TAG, "Widget ID $widgetId updated successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget ID $widgetId", e)
        }
    }
    
    override fun onEnabled(context: Context) {
        Log.d(TAG, "Widget enabled for the first time")
        // Start the update worker
        WeatherUpdateWorker.startPeriodicUpdate(context)
    }
    
    override fun onDisabled(context: Context) {
        Log.d(TAG, "All widget instances removed")
        // Stop the update worker
        WeatherUpdateWorker.cancelPeriodicUpdate(context)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        super.onReceive(context, intent)
    }
} 
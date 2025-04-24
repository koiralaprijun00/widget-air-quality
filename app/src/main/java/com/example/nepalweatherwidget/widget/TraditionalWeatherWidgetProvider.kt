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

class TraditionalWeatherWidgetProvider : AppWidgetProvider() {
    private val tag = "TraditionalWidget"

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(tag, "onUpdate called with ${appWidgetIds.size} widgets")
        
        // Update each widget
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId)
        }
    }
    
    private fun updateWidget(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        try {
            Log.d(tag, "Updating widget ID: $widgetId")
            
            // Get weather data
            val weatherData = GetWeatherUseCase.getMockWeatherData()
            Log.d(tag, "Got weather data: $weatherData")
            
            // Create a RemoteViews for the widget layout
            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            
            // Set the weather data
            views.setTextViewText(R.id.location, weatherData.location)
            views.setTextViewText(R.id.temperature, "${weatherData.temperature}°C")
            views.setTextViewText(R.id.weather_description, weatherData.description)
            views.setTextViewText(R.id.humidity, "${weatherData.humidity}%")
            views.setTextViewText(R.id.wind_speed, "${weatherData.windSpeed} m/s")
            
            // Set AQI data and description
            val aqiValue = getAirQualityIndex() // Get actual AQI value from data source
            views.setTextViewText(R.id.aqi_value, aqiValue.toString())
            
            // Set AQI description based on value
            val aqiDescription = when {
                aqiValue <= 50 -> "Good"
                aqiValue <= 100 -> "Moderate"
                aqiValue <= 150 -> "Unhealthy for Sensitive Groups"
                aqiValue <= 200 -> "Unhealthy"
                aqiValue <= 300 -> "Very Unhealthy"
                else -> "Hazardous"
            }
            views.setTextViewText(R.id.aqi_description, aqiDescription)
            
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
            Log.d(tag, "Widget ID $widgetId updated successfully")
        } catch (e: Exception) {
            Log.e(tag, "Error updating widget ID $widgetId", e)
        }
    }
    
    private fun getAirQualityIndex(): Int {
        // TODO: Implement actual AQI data fetching
        return 75 // Return a reasonable default value for now
    }
    
    override fun onEnabled(context: Context) {
        Log.d(tag, "Widget enabled for the first time")
        // Start the update worker
        WeatherUpdateWorker.startPeriodicUpdate(context)
    }
    
    override fun onDisabled(context: Context) {
        Log.d(tag, "All widget instances removed")
        // Stop the update worker
        WeatherUpdateWorker.cancelPeriodicUpdate(context)
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(tag, "Received intent: ${intent.action}")
        super.onReceive(context, intent)
    }
} 
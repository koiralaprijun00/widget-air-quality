package com.example.nepalweatherwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.nepalweatherwidget.MainActivity
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.util.ApiResult
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TraditionalWeatherWidgetProvider : AppWidgetProvider() {
    private val tag = "TraditionalWidget"
    
    @Inject
    lateinit var weatherRepository: WeatherRepository

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        coroutineScope.launch {
            when (val result = weatherRepository.getWeatherAndAirQuality("Kathmandu")) {
                is ApiResult.Success -> {
                    val (weather, airQuality) = result.data
                    updateWidget(context, appWidgetManager, appWidgetIds, weather, airQuality)
                }
                is ApiResult.Error -> {
                    showError(context, appWidgetManager, appWidgetIds, result.message)
                }
                is ApiResult.Loading -> {
                    showLoading(context, appWidgetManager, appWidgetIds)
                }
            }
        }
    }
    
    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        // Trigger a full update when widget size changes
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }
    
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        weather: WeatherData,
        airQuality: AirQuality
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_weather)
            
            views.setTextViewText(R.id.temperature, "${weather.temperature}°C")
            views.setTextViewText(R.id.description, weather.description)
            views.setTextViewText(R.id.humidity, "Humidity: ${weather.humidity}%")
            views.setTextViewText(R.id.wind_speed, "Wind: ${weather.windSpeed} m/s")
            views.setTextViewText(R.id.aqi, "AQI: ${airQuality.aqi}")
            views.setTextViewText(R.id.pm25, "PM2.5: ${airQuality.pm25}")
            views.setTextViewText(R.id.pm10, "PM10: ${airQuality.pm10}")

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun showError(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        message: String
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_weather)
            views.setTextViewText(R.id.temperature, "Error")
            views.setTextViewText(R.id.description, message)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun showLoading(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_weather)
            views.setTextViewText(R.id.temperature, "Loading...")
            views.setTextViewText(R.id.description, "Please wait")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
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
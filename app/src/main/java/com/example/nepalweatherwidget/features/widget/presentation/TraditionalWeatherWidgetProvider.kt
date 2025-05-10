package com.example.nepalweatherwidget.features.widget.presentation

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.WidgetPreferencesRepository
import com.example.nepalweatherwidget.features.widget.domain.repository.WidgetRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class TraditionalWeatherWidgetProvider : AppWidgetProvider() {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    @Inject
    lateinit var widgetPreferencesRepository: WidgetPreferencesRepository
    
    @Inject
    lateinit var widgetRepository: WidgetRepository
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        coroutineScope.launch {
            try {
                // Get widget preferences
                val preferences = widgetPreferencesRepository.getWidgetPreferences(appWidgetId)
                when (preferences) {
                    is Result.Success -> {
                        val locationName = preferences.data.locationName
                        // Get weather and air quality data
                        val result = weatherRepository.getWeatherAndAirQuality(locationName)
                        when (result) {
                            is Result.Success -> {
                                val (weatherData, airQuality) = result.data
                                updateWidgetViews(
                                    context,
                                    appWidgetManager,
                                    appWidgetId,
                                    weatherData,
                                    airQuality
                                )
                            }
                            is Result.Error -> {
                                showError(context, appWidgetManager, appWidgetId)
                            }
                        }
                    }
                    is Result.Error -> {
                        showError(context, appWidgetManager, appWidgetId)
                    }
                }
            } catch (e: Exception) {
                showError(context, appWidgetManager, appWidgetId)
            }
        }
    }
    
    private fun updateWidgetViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weatherData: WeatherData,
        airQuality: AirQuality
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_traditional_weather)
        
        // Update weather data
        views.setTextViewText(R.id.temperature, context.getString(R.string.temperature_format, weatherData.temperature))
        views.setTextViewText(R.id.weatherDescription, weatherData.description)
        views.setTextViewText(R.id.humidity, context.getString(R.string.humidity_format, weatherData.humidity))
        views.setTextViewText(R.id.windSpeed, context.getString(R.string.wind_speed_format, weatherData.windSpeed))
        
        // Update air quality data
        views.setTextViewText(R.id.aqiValue, context.getString(R.string.aqi_format, airQuality.aqi))
        views.setTextViewText(R.id.pm25Value, context.getString(R.string.pm25_format, airQuality.pm25))
        views.setTextViewText(R.id.pm10Value, context.getString(R.string.pm10_format, airQuality.pm10))
        
        // Update last update time
        val lastUpdate = SimpleDateFormat(context.getString(R.string.last_update_format), Locale.getDefault())
            .format(Date())
        views.setTextViewText(R.id.lastUpdate, context.getString(R.string.widget_last_update, lastUpdate))
        
        // Set refresh button click listener
        val refreshPendingIntent = getRefreshPendingIntent(context, appWidgetId)
        views.setOnClickPendingIntent(R.id.refreshButton, refreshPendingIntent)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun showError(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_traditional_weather)
        views.setTextViewText(R.id.temperature, context.getString(R.string.error_loading_weather))
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun getRefreshPendingIntent(context: Context, appWidgetId: Int): PendingIntent {
        val intent = Intent(context, TraditionalWeatherWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        return PendingIntent.getBroadcast(
            context,
            appWidgetId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel all coroutines when no widgets are active
        coroutineScope.cancel()
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_RETRY -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
                }
            }
            ACTION_REFRESH -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
                if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    val appWidgetManager = AppWidgetManager.getInstance(context)
                    onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
                }
            }
        }
    }
    
    companion object {
        private const val ACTION_RETRY = "com.example.nepalweatherwidget.ACTION_RETRY"
        private const val ACTION_REFRESH = "com.example.nepalweatherwidget.ACTION_REFRESH"
    }
} 
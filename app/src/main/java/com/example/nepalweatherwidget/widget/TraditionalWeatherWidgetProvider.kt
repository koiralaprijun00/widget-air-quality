package com.example.nepalweatherwidget.widget

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
import com.example.nepalweatherwidget.core.util.Logger
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class TraditionalWeatherWidgetProvider : AppWidgetProvider() {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Batch update widgets
        scope.launch {
            val widgetUpdates = appWidgetIds.map { widgetId ->
                async {
                    updateWidget(context, appWidgetManager, widgetId)
                }
            }
            widgetUpdates.awaitAll()
        }
    }
    
    private suspend fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int
    ) {
        // Show loading state immediately
        showLoading(context, appWidgetManager, widgetId)
        
        // Get data with timeout
        val result = withTimeoutOrNull(5000) {
            weatherRepository.getWeatherAndAirQualityByLocationName("Kathmandu")
        }
        
        if (result == null) {
            showError(context, appWidgetManager, widgetId, "Update timed out")
            return
        }
        
        when (result) {
            is Result.Success -> {
                val (weather, airQuality) = result.data
                updateWidgetViews(context, appWidgetManager, widgetId, weather, airQuality)
            }
            is Result.Error -> {
                showError(context, appWidgetManager, widgetId, result.exception.message ?: "Unknown error")
            }
        }
    }
    
    private fun updateWidgetViews(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        weather: WeatherData,
        airQuality: AirQuality
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_traditional_weather)
        
        // Update weather information
        views.setTextViewText(R.id.widget_temperature, context.getString(R.string.temperature_format, weather.temperature.toInt()))
        views.setTextViewText(R.id.widget_description, weather.description)
        views.setTextViewText(R.id.widget_humidity, context.getString(R.string.humidity_format, weather.humidity))
        views.setTextViewText(R.id.widget_wind_speed, context.getString(R.string.wind_speed_format, weather.windSpeed))
        
        // Update air quality information
        views.setTextViewText(R.id.widget_aqi, context.getString(R.string.aqi_format, airQuality.aqi))
        views.setTextViewText(R.id.widget_pm25, context.getString(R.string.pm25_format, airQuality.pm25))
        views.setTextViewText(R.id.widget_pm10, context.getString(R.string.pm10_format, airQuality.pm10))
        
        // Update last update time
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val lastUpdateTime = dateFormat.format(Date())
        views.setTextViewText(R.id.widget_last_update, context.getString(R.string.last_update_format, lastUpdateTime))
        
        // Add refresh action
        val refreshIntent = Intent(context, TraditionalWeatherWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_refresh, refreshPendingIntent)
        
        appWidgetManager.updateAppWidget(widgetId, views)
    }
    
    private fun showLoading(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_loading)
        appWidgetManager.updateAppWidget(widgetId, views)
    }
    
    private fun showError(context: Context, appWidgetManager: AppWidgetManager, widgetId: Int, errorMessage: String) {
        val views = RemoteViews(context.packageName, R.layout.widget_layout_error)
        views.setTextViewText(R.id.error_message, errorMessage)
        
        // Add retry action
        val retryIntent = Intent(context, TraditionalWeatherWidgetProvider::class.java).apply {
            action = ACTION_RETRY
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val retryPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId,
            retryIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.retry_button, retryPendingIntent)
        
        appWidgetManager.updateAppWidget(widgetId, views)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Cancel all coroutines when no widgets are active
        scope.cancel()
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
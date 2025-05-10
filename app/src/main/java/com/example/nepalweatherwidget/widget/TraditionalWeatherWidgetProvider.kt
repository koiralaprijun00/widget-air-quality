package com.example.nepalweatherwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.RemoteViews
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.WidgetRepository
import com.example.nepalweatherwidget.ui.WidgetConfigActivity
import com.example.nepalweatherwidget.core.util.Logger
import com.example.nepalweatherwidget.worker.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt
import androidx.core.graphics.blue
import androidx.core.graphics.green
import androidx.core.graphics.red

@AndroidEntryPoint
class TraditionalWeatherWidgetProvider : AppWidgetProvider() {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    @Inject
    lateinit var widgetRepository: WidgetRepository
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Show loading initially
        appWidgetIds.forEach { widgetId ->
            showLoading(context, appWidgetManager, widgetId)
        }
        
        scope.launch {
            try {
                // Get cached data first for immediate display
                val cachedData = widgetRepository.getWidgetData()
                if (cachedData is com.example.nepalweatherwidget.domain.model.WidgetData.Success) {
                    updateWidgets(
                        context, 
                        appWidgetManager, 
                        appWidgetIds, 
                        cachedData.weather, 
                        cachedData.airQuality
                    )
                }
                
                // Fetch fresh data
                appWidgetIds.forEach { widgetId ->
                    val location = widgetRepository.getWidgetLocation(widgetId)
                    
                    when (val result = weatherRepository.getWeatherAndAirQualityByLocationName(location)) {
                        is Result.Success -> {
                            val (weather, airQuality) = result.data
                            updateWidget(context, appWidgetManager, widgetId, weather, airQuality)
                        }
                        is Result.Error -> {
                            // Show cached data with error indicator if available
                            when (cachedData) {
                                is com.example.nepalweatherwidget.domain.model.WidgetData.Success -> {
                                    updateWidget(
                                        context, 
                                        appWidgetManager, 
                                        widgetId, 
                                        cachedData.weather, 
                                        cachedData.airQuality,
                                        stale = true
                                    )
                                }
                                else -> showError(context, appWidgetManager, widgetId, result.exception.message ?: "Unknown error")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.e("Widget update error", e)
                appWidgetIds.forEach { widgetId ->
                    showError(context, appWidgetManager, widgetId, e.message ?: "Unknown error")
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
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
    }
    
    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        WidgetUpdateWorker.schedule(context)
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        WidgetUpdateWorker.cancel(context)
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
                    showLoading(context, appWidgetManager, appWidgetId)
                    onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
                }
            }
        }
    }

    companion object {
        private const val ACTION_RETRY = "com.example.nepalweatherwidget.ACTION_RETRY"
        private const val ACTION_REFRESH = "com.example.nepalweatherwidget.ACTION_REFRESH"
        
        // Widget size thresholds
        private const val SMALL_WIDTH_THRESHOLD = 200
        private const val MEDIUM_WIDTH_THRESHOLD = 300
        private const val SMALL_HEIGHT_THRESHOLD = 120
        private const val MEDIUM_HEIGHT_THRESHOLD = 200
        
        fun updateWidgetNow(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, TraditionalWeatherWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
            }
            context.sendBroadcast(intent)
        }
    }
    
    private fun updateWidgets(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        weather: WeatherData,
        airQuality: AirQuality
    ) {
        appWidgetIds.forEach { widgetId ->
            updateWidget(context, appWidgetManager, widgetId, weather, airQuality)
        }
    }
    
    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        widgetId: Int,
        weather: WeatherData,
        airQuality: AirQuality,
        stale: Boolean = false
    ) {
        val options = appWidgetManager.getAppWidgetOptions(widgetId)
        val layout = getLayoutForSize(options)
        val views = RemoteViews(context.packageName, layout)
        
        // Update UI
        updateWidgetUI(views, layout, weather, airQuality, stale)
        
        // Add click actions
        addClickActions(context, views, widgetId)
        
        appWidgetManager.updateAppWidget(widgetId, views)
    }
    
    private fun getLayoutForSize(options: Bundle): Int {
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
        val maxWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
        val maxHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT)
        
        Logger.d("Widget size: min=${minWidth}x${minHeight}, max=${maxWidth}x${maxHeight}")
        
        return when {
            // Extra small (2x1)
            minWidth < SMALL_WIDTH_THRESHOLD || minHeight < SMALL_HEIGHT_THRESHOLD -> {
                R.layout.widget_layout_extra_small
            }
            // Small (3x2)
            minWidth < MEDIUM_WIDTH_THRESHOLD && minHeight < MEDIUM_HEIGHT_THRESHOLD -> {
                R.layout.widget_layout_small
            }
            // Medium (4x2)
            minWidth < MEDIUM_WIDTH_THRESHOLD || minHeight < MEDIUM_HEIGHT_THRESHOLD -> {
                R.layout.widget_layout_medium
            }
            // Large (4x3 and bigger)
            else -> R.layout.widget_layout_large
        }
    }
    
    private fun updateWidgetUI(
        views: RemoteViews,
        layoutId: Int,
        weather: WeatherData,
        airQuality: AirQuality,
        stale: Boolean = false
    ) {
        val location = widgetRepository.getWidgetLocation()
        
        views.apply {
            // Common elements present in all layouts
            setTextViewText(R.id.location, location)
            setTextViewText(R.id.temperature, "${weather.temperature.roundToInt()}°C")
            
            // AQI values - present in all layouts
            setTextViewText(R.id.aqi_value, airQuality.aqi.toString())
            
            // Handle stale data indicator
            setViewVisibility(R.id.stale_indicator, if (stale) android.view.View.VISIBLE else android.view.View.GONE)
            
            // Set colors based on AQI
            val aqiColor = getAQIColor(airQuality.aqi)
            setInt(R.id.aqi_indicator, "setBackgroundColor", aqiColor)
            setInt(R.id.aqi_value, "setTextColor", Color.WHITE)
            
            // Set last updated time
            val lastUpdated = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            setTextViewText(R.id.last_updated, "Last updated: $lastUpdated")
        }
    }
    
    private fun getAQIColor(aqi: Int): Int {
        return when (aqi) {
            1 -> Color.rgb(0, 228, 0) // Good
            2 -> Color.rgb(255, 255, 0) // Fair
            3 -> Color.rgb(255, 126, 0) // Moderate
            4 -> Color.rgb(255, 0, 0) // Poor
            5 -> Color.rgb(153, 0, 76) // Very Poor
            else -> Color.GRAY
        }
    }
    
    private fun addClickActions(context: Context, views: RemoteViews, widgetId: Int) {
        // Retry action
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
        
        // Refresh action
        val refreshIntent = Intent(context, TraditionalWeatherWidgetProvider::class.java).apply {
            action = ACTION_REFRESH
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context,
            widgetId + 1000, // Different request code
            refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.refresh_button, refreshPendingIntent)
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
} 
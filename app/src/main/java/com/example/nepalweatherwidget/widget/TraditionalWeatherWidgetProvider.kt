package com.example.nepalweatherwidget.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import com.example.nepalweatherwidget.MainActivity
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.domain.model.WidgetData
import com.example.nepalweatherwidget.domain.repository.WidgetRepository
import com.example.nepalweatherwidget.ui.WidgetConfigActivity
import com.example.nepalweatherwidget.worker.WidgetUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TraditionalWeatherWidgetProvider : AppWidgetProvider() {
    private val tag = "TraditionalWidget"
    
    @Inject
    lateinit var widgetRepository: WidgetRepository
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        scope.launch {
            showLoading(context, appWidgetManager, appWidgetIds)
            
            when (val data = widgetRepository.getWidgetData()) {
                is WidgetData.Success -> {
                    updateWidgets(context, appWidgetManager, appWidgetIds, data)
                }
                is WidgetData.Error -> {
                    showError(context, appWidgetManager, appWidgetIds, data.message)
                }
                WidgetData.Loading -> {
                    // Already showing loading
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
        
        if (intent.action == ACTION_RETRY) {
            val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            if (appWidgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                onUpdate(context, appWidgetManager, intArrayOf(appWidgetId))
            }
        }
    }

    companion object {
        private const val ACTION_RETRY = "com.example.nepalweatherwidget.ACTION_RETRY"
        
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
        data: WidgetData.Success
    ) {
        appWidgetIds.forEach { widgetId ->
            val options = appWidgetManager.getAppWidgetOptions(widgetId)
            val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
            val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT)
            
            val layout = getLayoutForSize(minWidth, minHeight)
            val views = RemoteViews(context.packageName, layout)
            
            // Update UI
            updateWidgetUI(views, data.weather, data.airQuality)
            
            // Add click actions
            addClickActions(context, views)
            
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
    
    private fun updateWidgetUI(
        views: RemoteViews,
        weather: WeatherData,
        airQuality: AirQuality
    ) {
        views.apply {
            setTextViewText(R.id.location, widgetRepository.getWidgetLocation())
            setTextViewText(R.id.temperature, "${weather.temperature.toInt()}°C")
            setTextViewText(R.id.description, weather.description)
            
            // Medium and large layouts
            if (findViewId(R.id.humidity) != 0) {
                setTextViewText(R.id.humidity, "Humidity: ${weather.humidity}%")
                setTextViewText(R.id.wind_speed, "Wind: ${weather.windSpeed} m/s")
                setTextViewText(R.id.aqi, "AQI: ${airQuality.aqi}")
                
                // Set AQI color
                val aqiColor = getAqiColor(airQuality.aqi)
                setInt(R.id.aqi, "setTextColor", aqiColor)
            }
            
            // Large layout only
            if (findViewId(R.id.pm25) != 0) {
                setTextViewText(R.id.pm25, "PM2.5: ${airQuality.pm25}")
                setTextViewText(R.id.pm10, "PM10: ${airQuality.pm10}")
            }
        }
    }
    
    private fun addClickActions(context: Context, views: RemoteViews) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
    }
    
    private fun getLayoutForSize(width: Int, height: Int): Int {
        return when {
            width < 200 && height < 120 -> R.layout.widget_layout_small
            width < 300 || height < 150 -> R.layout.widget_layout_medium
            else -> R.layout.widget_layout_large
        }
    }
    
    private fun getAqiColor(aqi: Int): Int {
        return when (aqi) {
            1 -> Color.parseColor("#4CAF50") // Green
            2 -> Color.parseColor("#FFEB3B") // Yellow
            3 -> Color.parseColor("#FF9800") // Orange
            4 -> Color.parseColor("#F44336") // Red
            5 -> Color.parseColor("#9C27B0") // Purple
            else -> Color.parseColor("#9E9E9E") // Gray
        }
    }
    
    private fun showError(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
        message: String
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_layout_error)
            
            // Set error message
            views.setTextViewText(R.id.error_message, message)
            
            // Add retry action
            val retryIntent = Intent(context, TraditionalWeatherWidgetProvider::class.java).apply {
                action = ACTION_RETRY
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val retryPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                retryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.retry_button, retryPendingIntent)
            
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }

    private fun showLoading(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            val views = RemoteViews(context.packageName, R.layout.widget_layout_loading)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
} 
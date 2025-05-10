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
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
    
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        try {
            val prefs = context.getSharedPreferences(WidgetUpdateWorker.WIDGET_PREFS, Context.MODE_PRIVATE)
            
            // Get stored data
            val temperature = prefs.getFloat(WidgetUpdateWorker.KEY_TEMPERATURE, 0f)
            val description = prefs.getString(WidgetUpdateWorker.KEY_DESCRIPTION, "N/A") ?: "N/A"
            val humidity = prefs.getInt(WidgetUpdateWorker.KEY_HUMIDITY, 0)
            val windSpeed = prefs.getFloat(WidgetUpdateWorker.KEY_WIND_SPEED, 0f)
            val aqi = prefs.getInt(WidgetUpdateWorker.KEY_AQI, 0)
            val pm25 = prefs.getFloat(WidgetUpdateWorker.KEY_PM25, 0f)
            val pm10 = prefs.getFloat(WidgetUpdateWorker.KEY_PM10, 0f)
            val lastUpdate = prefs.getLong(WidgetUpdateWorker.KEY_LAST_UPDATE, 0L)
            
            // Create RemoteViews
            val views = RemoteViews(context.packageName, R.layout.widget_traditional_weather)
            
            // Update weather information
            views.setTextViewText(R.id.widget_temperature, context.getString(R.string.temperature_format, temperature.toInt()))
            views.setTextViewText(R.id.widget_description, description)
            views.setTextViewText(R.id.widget_humidity, context.getString(R.string.humidity_format, humidity))
            views.setTextViewText(R.id.widget_wind_speed, context.getString(R.string.wind_speed_format, windSpeed))
            
            // Update air quality information
            views.setTextViewText(R.id.widget_aqi, context.getString(R.string.aqi_format, aqi))
            views.setTextViewText(R.id.widget_pm25, context.getString(R.string.pm25_format, pm25))
            views.setTextViewText(R.id.widget_pm10, context.getString(R.string.pm10_format, pm10))
            
            // Update last update time
            val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val lastUpdateTime = dateFormat.format(Date(lastUpdate))
            views.setTextViewText(R.id.widget_last_update, context.getString(R.string.last_update_format, lastUpdateTime))
            
            // Update widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
            
            Logger.d("TraditionalWeatherWidgetProvider: Successfully updated widget $appWidgetId")
        } catch (e: Exception) {
            Logger.e("TraditionalWeatherWidgetProvider: Error updating widget $appWidgetId", e)
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
        Logger.d("TraditionalWeatherWidgetProvider: Widget enabled")
    }
    
    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        Logger.d("TraditionalWeatherWidgetProvider: Widget disabled")
    }
    
    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        Logger.d("TraditionalWeatherWidgetProvider: Widgets deleted: ${appWidgetIds.joinToString()}")
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
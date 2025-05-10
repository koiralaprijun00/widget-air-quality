package com.example.nepalweatherwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.core.util.Logger
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(appContext, workerParams) {

    @AssistedFactory
    interface Factory : WorkerAssistedFactory<WidgetUpdateWorker>

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get widget IDs
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val widgetComponent = ComponentName(applicationContext, TraditionalWeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

            if (appWidgetIds.isEmpty()) {
                Logger.d("WidgetUpdateWorker: No widgets to update")
                return@withContext Result.success()
            }

            // Show loading state
            showLoadingInWidgets(appWidgetManager, appWidgetIds)

            // Try to get fresh data with timeout
            val result = withTimeoutOrNull(30_000) {
                weatherRepository.getWeatherAndAirQualityByLocationName("Kathmandu")
            } ?: run {
                Logger.e("WidgetUpdateWorker: Timeout fetching weather data")
                showErrorInWidgets(appWidgetManager, appWidgetIds, "Update timeout")
                return@withContext Result.retry()
            }

            when (result) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    
                    // Update widget data
                    updateWidgetData(weather, airQuality)
                    
                    // Trigger widget update
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_layout)
                    
                    Logger.d("WidgetUpdateWorker: Successfully updated widgets")
                    Result.success()
                }
                is Result.Error -> {
                    Logger.e("WidgetUpdateWorker: Error fetching weather data", result.exception)
                    
                    // Try to get cached data as fallback
                    val cachedData = getCachedData()
                    if (cachedData != null) {
                        Logger.d("WidgetUpdateWorker: Using cached data as fallback")
                        updateWidgetData(cachedData.first, cachedData.second)
                        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_layout)
                        Result.success()
                    } else {
                        showErrorInWidgets(appWidgetManager, appWidgetIds, "Failed to update")
                        Result.retry()
                    }
                }
            }
        } catch (e: Exception) {
            Logger.e("WidgetUpdateWorker: Unexpected error", e)
            showErrorInWidgets(
                AppWidgetManager.getInstance(applicationContext),
                AppWidgetManager.getInstance(applicationContext).getAppWidgetIds(
                    ComponentName(applicationContext, TraditionalWeatherWidgetProvider::class.java)
                ),
                "Unexpected error"
            )
            Result.retry()
        }
    }
    
    private fun showLoadingInWidgets(appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout_loading)
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
    
    private fun showErrorInWidgets(appWidgetManager: AppWidgetManager, appWidgetIds: IntArray, errorMessage: String) {
        appWidgetIds.forEach { widgetId ->
            val views = RemoteViews(applicationContext.packageName, R.layout.widget_layout_error)
            views.setTextViewText(R.id.error_message, errorMessage)
            
            // Add retry action
            val retryIntent = Intent(applicationContext, TraditionalWeatherWidgetProvider::class.java).apply {
                action = "com.example.nepalweatherwidget.ACTION_RETRY"
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
            }
            val retryPendingIntent = PendingIntent.getBroadcast(
                applicationContext,
                widgetId,
                retryIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.retry_button, retryPendingIntent)
            
            appWidgetManager.updateAppWidget(widgetId, views)
        }
    }
    
    private fun updateWidgetData(weather: WeatherData, airQuality: AirQuality) {
        // Store widget data in SharedPreferences with timestamp
        applicationContext.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE).edit().apply {
            putFloat(KEY_TEMPERATURE, weather.temperature.toFloat())
            putString(KEY_DESCRIPTION, weather.description)
            putInt(KEY_HUMIDITY, weather.humidity)
            putFloat(KEY_WIND_SPEED, weather.windSpeed.toFloat())
            putInt(KEY_AQI, airQuality.aqi)
            putFloat(KEY_PM25, airQuality.pm25.toFloat())
            putFloat(KEY_PM10, airQuality.pm10.toFloat())
            putLong(KEY_LAST_UPDATE, System.currentTimeMillis())
            apply()
        }
    }
    
    private fun getCachedData(): Pair<WeatherData, AirQuality>? {
        val prefs = applicationContext.getSharedPreferences(WIDGET_PREFS, Context.MODE_PRIVATE)
        val lastUpdate = prefs.getLong(KEY_LAST_UPDATE, 0L)
        
        // Check if cache is still valid (within 30 minutes)
        if (System.currentTimeMillis() - lastUpdate > TimeUnit.MINUTES.toMillis(30)) {
            return null
        }
        
        return try {
            val weather = WeatherData(
                temperature = prefs.getFloat(KEY_TEMPERATURE, 0f).toDouble(),
                description = prefs.getString(KEY_DESCRIPTION, "") ?: "",
                humidity = prefs.getInt(KEY_HUMIDITY, 0),
                windSpeed = prefs.getFloat(KEY_WIND_SPEED, 0f).toDouble()
            )
            
            val airQuality = AirQuality(
                aqi = prefs.getInt(KEY_AQI, 0),
                pm25 = prefs.getFloat(KEY_PM25, 0f).toDouble(),
                pm10 = prefs.getFloat(KEY_PM10, 0f).toDouble()
            )
            
            Pair(weather, airQuality)
        } catch (e: Exception) {
            Logger.e("WidgetUpdateWorker: Error reading cached data", e)
            null
        }
    }
    
    companion object {
        const val WIDGET_PREFS = "widget_prefs"
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_DESCRIPTION = "description"
        const val KEY_HUMIDITY = "humidity"
        const val KEY_WIND_SPEED = "wind_speed"
        const val KEY_AQI = "aqi"
        const val KEY_PM25 = "pm25"
        const val KEY_PM10 = "pm10"
        const val KEY_LAST_UPDATE = "last_update"
    }
} 
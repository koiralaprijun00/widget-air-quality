package com.example.nepalweatherwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
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

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(appContext, workerParams) {

    @AssistedFactory
    interface Factory : WorkerAssistedFactory<WidgetUpdateWorker>

    override suspend fun doWork(): androidx.work.Result = withContext(Dispatchers.IO) {
        try {
            // Get widget IDs
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val widgetComponent = ComponentName(applicationContext, TraditionalWeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

            if (appWidgetIds.isEmpty()) {
                Logger.d("WidgetUpdateWorker: No widgets to update")
                return@withContext androidx.work.Result.success()
            }

            // Get weather data
            val result = weatherRepository.getWeatherAndAirQualityByLocationName("Kathmandu")
            
            when (result) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    
                    // Update widget data
                    updateWidgetData(weather, airQuality)
                    
                    // Trigger widget update
                    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_layout)
                    
                    Logger.d("WidgetUpdateWorker: Successfully updated widgets")
                    androidx.work.Result.success()
                }
                is Result.Error -> {
                    Logger.e("WidgetUpdateWorker: Error fetching weather data", result.exception)
                    androidx.work.Result.retry()
                }
            }
        } catch (e: Exception) {
            Logger.e("WidgetUpdateWorker: Unexpected error", e)
            androidx.work.Result.retry()
        }
    }
    
    private fun updateWidgetData(weather: WeatherData, airQuality: AirQuality) {
        // Store widget data in SharedPreferences or other persistent storage
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
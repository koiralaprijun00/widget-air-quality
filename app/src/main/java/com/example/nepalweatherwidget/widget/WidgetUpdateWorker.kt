package com.example.nepalweatherwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WidgetUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get widget IDs
            val appWidgetManager = AppWidgetManager.getInstance(applicationContext)
            val widgetComponent = ComponentName(applicationContext, WeatherWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(widgetComponent)

            if (appWidgetIds.isEmpty()) {
                return@withContext Result.success()
            }

            // Get weather data
            val result = weatherRepository.getWeatherAndAirQuality("Kathmandu")
            
            when (result) {
                is com.example.nepalweatherwidget.domain.model.ApiResult.Success -> {
                    val (weather, airQuality) = result.data
                    
                    // Update all widgets
                    appWidgetIds.forEach { widgetId ->
                        WeatherWidgetProvider.updateAppWidget(
                            applicationContext,
                            appWidgetManager,
                            widgetId,
                            weather,
                            airQuality
                        )
                    }
                    Result.success()
                }
                is com.example.nepalweatherwidget.domain.model.ApiResult.Error -> {
                    Result.retry()
                }
                is com.example.nepalweatherwidget.domain.model.ApiResult.Loading -> {
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Result.retry()
        }
    }
} 
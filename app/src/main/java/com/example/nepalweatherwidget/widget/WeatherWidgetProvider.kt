package com.example.nepalweatherwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.domain.model.AirQualityData
import com.example.nepalweatherwidget.domain.model.WeatherData
import java.util.concurrent.TimeUnit

class WeatherWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Schedule periodic updates using WorkManager
        val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES // flex interval
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            updateRequest
        )

        // Trigger immediate update
        appWidgetIds.forEach { appWidgetId ->
            updateAppWidget(context, appWidgetManager, appWidgetId, null, null)
        }
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        // Start periodic updates when first widget is added
        val updateRequest = PeriodicWorkRequestBuilder<WidgetUpdateWorker>(
            15, TimeUnit.MINUTES,
            5, TimeUnit.MINUTES
        )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WIDGET_UPDATE_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            updateRequest
        )
    }

    override fun onDisabled(context: Context) {
        super.onDisabled(context)
        // Stop periodic updates when last widget is removed
        WorkManager.getInstance(context).cancelUniqueWork(WIDGET_UPDATE_WORK_NAME)
    }

    companion object {
        private const val WIDGET_UPDATE_WORK_NAME = "widget_update"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            weather: WeatherData?,
            airQuality: AirQualityData?
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_weather)
            
            weather?.let {
                views.setTextViewText(R.id.widget_temperature, "${it.temperature}°C")
                views.setTextViewText(R.id.widget_description, it.description)
                views.setTextViewText(R.id.widget_humidity, "${it.humidity}%")
                views.setTextViewText(R.id.widget_wind_speed, "${it.windSpeed} m/s")
            }

            airQuality?.let {
                views.setTextViewText(R.id.widget_aqi, "AQI: ${it.aqi}")
                views.setTextViewText(R.id.widget_pm25, "PM2.5: ${it.pm25}")
                views.setTextViewText(R.id.widget_pm10, "PM10: ${it.pm10}")
            }

            // Update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
} 
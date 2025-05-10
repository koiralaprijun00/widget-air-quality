package com.example.nepalweatherwidget.presentation.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TraditionalWeatherWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var weatherRepository: WeatherRepository

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        coroutineScope.launch {
            showLoading(context, appWidgetManager, appWidgetIds)
            
            weatherRepository.getWeatherAndAirQuality("Kathmandu")
                .onSuccess { (weather, airQuality) ->
                    updateWidget(context, appWidgetManager, appWidgetIds, weather, airQuality)
                }
                .onFailure { exception ->
                    showError(context, appWidgetManager, appWidgetIds, exception.message ?: "Unknown error")
                }
        }
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
            
            views.setTextViewText(R.id.widget_temperature, "${weather.temperature}°C")
            views.setTextViewText(R.id.widget_description, weather.description)
            views.setTextViewText(R.id.widget_humidity, "Humidity: ${weather.humidity}%")
            views.setTextViewText(R.id.widget_wind_speed, "Wind: ${weather.windSpeed} m/s")
            views.setTextViewText(R.id.widget_aqi, "AQI: ${airQuality.aqi}")
            views.setTextViewText(R.id.widget_pm25, "PM2.5: ${airQuality.pm25}")
            views.setTextViewText(R.id.widget_pm10, "PM10: ${airQuality.pm10}")

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
            views.setTextViewText(R.id.widget_temperature, "Error")
            views.setTextViewText(R.id.widget_description, message)
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
            views.setTextViewText(R.id.widget_temperature, "Loading...")
            views.setTextViewText(R.id.widget_description, "Please wait")
            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
} 
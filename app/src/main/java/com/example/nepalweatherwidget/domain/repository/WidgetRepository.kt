package com.example.nepalweatherwidget.domain.repository

import android.content.SharedPreferences
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.domain.model.WidgetData
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WidgetRepository @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val weatherDao: WeatherDao,
    private val airQualityDao: AirQualityDao,
    private val preferences: SharedPreferences
) {
    suspend fun getWidgetData(): WidgetData {
        return try {
            val location = preferences.getString("widget_location", "Kathmandu") ?: "Kathmandu"
            when (val result = weatherRepository.getWeatherAndAirQuality(location)) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    WidgetData.Success(weather, airQuality)
                }
                is Result.Error -> {
                    // Try to get cached data
                    getCachedData() ?: WidgetData.Error(result.exception.message ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            getCachedData() ?: WidgetData.Error(e.message ?: "Unknown error")
        }
    }
    
    private suspend fun getCachedData(): WidgetData.Success? {
        return try {
            val weatherEntity = weatherDao.getLatestWeatherData().first()
            val airQualityEntity = airQualityDao.getLatestAirQualityData().first()
            
            if (weatherEntity != null && airQualityEntity != null) {
                WidgetData.Success(
                    weather = weatherEntity.toWeatherData(),
                    airQuality = airQualityEntity.toAirQuality()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    fun updateWidgetLocation(location: String) {
        preferences.edit().putString("widget_location", location).apply()
    }

    fun getWidgetLocation(): String {
        return preferences.getString("widget_location", "Kathmandu") ?: "Kathmandu"
    }
} 
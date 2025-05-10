package com.example.nepalweatherwidget.domain.repository

import android.content.SharedPreferences
import android.util.Log
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
    
    companion object {
        private const val TAG = "WidgetRepository"
        private const val PREF_WIDGET_LOCATION = "widget_location"
        private const val PREF_WIDGET_LOCATION_PREFIX = "widget_location_"
        private const var PREF_DEFAULT_LOCATION = "default_location"
        private const var PREF_LAST_UPDATE_TIME = "last_update_time"
        private const val CACHE_VALIDITY_MINUTES = 15
    }
    
    /**
     * Get widget data with caching strategy
     */
    suspend fun getWidgetData(widgetId: Int? = null): WidgetData {
        return try {
            val location = getWidgetLocation(widgetId)
            
            // Check cache validity
            if (isCacheValid()) {
                getCachedData()?.let { cached ->
                    Log.d(TAG, "Using cached data for widget $widgetId")
                    return cached
                }
            }
            
            // Fetch fresh data
            Log.d(TAG, "Fetching fresh data for widget $widgetId")
            when (val result = weatherRepository.getWeatherAndAirQualityByLocationName(location)) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    updateLastUpdateTime()
                    WidgetData.Success(weather, airQuality)
                }
                is Result.Error -> {
                    // Try to use cached data on error
                    getCachedData() ?: WidgetData.Error(result.exception.message ?: "Unknown error")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting widget data", e)
            getCachedData() ?: WidgetData.Error(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Get cached data from local database
     */
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
            Log.e(TAG, "Error getting cached data", e)
            null
        }
    }
    
    /**
     * Check if cached data is still valid
     */
    private fun isCacheValid(): Boolean {
        val lastUpdate = preferences.getLong(PREF_LAST_UPDATE_TIME, 0)
        val currentTime = System.currentTimeMillis()
        val timeDiff = currentTime - lastUpdate
        return timeDiff < (CACHE_VALIDITY_MINUTES * 60 * 1000)
    }
    
    /**
     * Update the last update time
     */
    private fun updateLastUpdateTime() {
        preferences.edit()
            .putLong(PREF_LAST_UPDATE_TIME, System.currentTimeMillis())
            .apply()
    }
    
    /**
     * Update widget location for a specific widget
     */
    fun updateWidgetLocation(widgetId: Int, location: String) {
        preferences.edit()
            .putString("$PREF_WIDGET_LOCATION_PREFIX$widgetId", location)
            .apply()
    }
    
    /**
     * Get widget location for a specific widget
     */
    fun getWidgetLocation(widgetId: Int? = null): String {
        return if (widgetId != null) {
            preferences.getString(
                "$PREF_WIDGET_LOCATION_PREFIX$widgetId",
                getDefaultLocation()
            ) ?: getDefaultLocation()
        } else {
            getDefaultLocation()
        }
    }
    
    /**
     * Get the default location
     */
    fun getDefaultLocation(): String {
        return preferences.getString(PREF_DEFAULT_LOCATION, "Kathmandu") ?: "Kathmandu"
    }
    
    /**
     * Set the default location
     */
    fun setDefaultLocation(location: String) {
        preferences.edit()
            .putString(PREF_DEFAULT_LOCATION, location)
            .apply()
    }
    
    /**
     * Remove widget location when widget is deleted
     */
    fun removeWidgetLocation(widgetId: Int) {
        preferences.edit()
            .remove("$PREF_WIDGET_LOCATION_PREFIX$widgetId")
            .apply()
    }
    
    /**
     * Clear all widget data (for debugging/testing)
     */
    fun clearAllWidgetData() {
        preferences.edit()
            .clear()
            .apply()
    }
} 
package com.example.nepalweatherwidget.data.cache

import android.content.Context
import androidx.room.Room
import com.example.nepalweatherwidget.data.db.AppDatabase
import com.example.nepalweatherwidget.data.db.entity.AirQualityEntity
import com.example.nepalweatherwidget.data.db.entity.WeatherEntity
import com.example.nepalweatherwidget.data.mapper.toAirQuality
import com.example.nepalweatherwidget.data.mapper.toWeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.Location
import com.example.nepalweatherwidget.domain.model.WeatherData
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherCache @Inject constructor(
    private val context: Context
) {
    private val database = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "weather_database"
    ).build()

    private val weatherDao = database.weatherDao()
    private val airQualityDao = database.airQualityDao()
    private val locationDao = database.locationDao()

    companion object {
        private const val CACHE_VALIDITY_MINUTES = 15L
        private const val STALE_DATA_THRESHOLD_HOURS = 2L
    }

    suspend fun getWeatherData(locationName: String): WeatherData? {
        val entity = weatherDao.getWeatherData(locationName).first()
        return if (entity != null && isDataValid(entity.timestamp)) {
            entity.toWeatherData()
        } else {
            null
        }
    }

    suspend fun getAirQuality(locationName: String): AirQuality? {
        val entity = airQualityDao.getAirQualityData(locationName).first()
        return if (entity != null && isDataValid(entity.timestamp)) {
            entity.toAirQuality()
        } else {
            null
        }
    }

    suspend fun cacheWeatherData(locationName: String, weatherData: WeatherData) {
        val entity = WeatherEntity.fromWeatherData(weatherData, locationName)
        weatherDao.insertWeatherData(entity)
    }

    suspend fun cacheAirQuality(locationName: String, airQuality: AirQuality) {
        val entity = AirQualityEntity.fromAirQuality(airQuality, locationName)
        airQualityDao.insertAirQualityData(entity)
    }

    suspend fun getSavedLocations(): List<Location> {
        return locationDao.getAllLocations().first().map { it.toLocation() }
    }

    suspend fun saveLocation(location: Location) {
        locationDao.insertLocation(location.toEntity())
    }

    suspend fun deleteLocation(locationId: String) {
        locationDao.deleteLocation(locationId)
    }

    suspend fun clear() {
        weatherDao.deleteAllWeatherData()
        airQualityDao.deleteAllAirQualityData()
        locationDao.deleteAllLocations()
    }

    private fun isDataValid(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age < TimeUnit.MINUTES.toMillis(CACHE_VALIDITY_MINUTES)
    }

    private fun isDataStale(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age < TimeUnit.HOURS.toMillis(STALE_DATA_THRESHOLD_HOURS)
    }
} 
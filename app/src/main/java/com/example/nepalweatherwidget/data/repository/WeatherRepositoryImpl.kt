package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.core.cache.WeatherCache
import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.extension.withNetworkRetry
import com.example.nepalweatherwidget.core.network.NetworkMonitor
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.core.util.Logger
import com.example.nepalweatherwidget.data.remote.api.WeatherService
import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.data.local.entity.AirQualityEntity
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.GeocodingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import com.example.nepalweatherwidget.data.api.WeatherApi
import com.example.nepalweatherwidget.data.cache.WeatherCache
import com.example.nepalweatherwidget.data.mapper.toAirQuality
import com.example.nepalweatherwidget.data.mapper.toWeatherData
import com.example.nepalweatherwidget.domain.model.Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val api: WeatherApi,
    private val cache: WeatherCache
) : WeatherRepository {
    
    override suspend fun getWeatherData(locationName: String): Result<WeatherData> = withContext(Dispatchers.IO) {
        try {
            // Try to get from cache first
            cache.getWeatherData(locationName)?.let {
                return@withContext Result.success(it)
            }

            // If not in cache, fetch from API
            val response = api.getWeatherData(locationName)
            val weatherData = response.toWeatherData()
            
            // Cache the result
            cache.cacheWeatherData(locationName, weatherData)
            
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAirQuality(locationName: String): Result<AirQuality> = withContext(Dispatchers.IO) {
        try {
            // Try to get from cache first
            cache.getAirQuality(locationName)?.let {
                return@withContext Result.success(it)
            }

            // If not in cache, fetch from API
            val response = api.getAirQuality(locationName)
            val airQuality = response.toAirQuality()
            
            // Cache the result
            cache.cacheAirQuality(locationName, airQuality)
            
            Result.success(airQuality)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWeatherAndAirQuality(locationName: String): Result<Pair<WeatherData, AirQuality>> = 
        withContext(Dispatchers.IO) {
            try {
                val weatherResult = getWeatherData(locationName)
                val airQualityResult = getAirQuality(locationName)

                if (weatherResult.isSuccess && airQualityResult.isSuccess) {
                    Result.success(Pair(weatherResult.getOrNull()!!, airQualityResult.getOrNull()!!))
                } else {
                    Result.failure(Exception("Failed to get weather and air quality data"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getWeatherDataByCoordinates(lat: Double, lon: Double): Result<WeatherData> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.getWeatherDataByCoordinates(lat, lon)
                Result.success(response.toWeatherData())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQuality> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.getAirQualityByCoordinates(lat, lon)
                Result.success(response.toAirQuality())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getWeatherAndAirQualityByCoordinates(lat: Double, lon: Double): Result<Pair<WeatherData, AirQuality>> = 
        withContext(Dispatchers.IO) {
            try {
                val weatherResult = getWeatherDataByCoordinates(lat, lon)
                val airQualityResult = getAirQualityByCoordinates(lat, lon)

                if (weatherResult.isSuccess && airQualityResult.isSuccess) {
                    Result.success(Pair(weatherResult.getOrNull()!!, airQualityResult.getOrNull()!!))
                } else {
                    Result.failure(Exception("Failed to get weather and air quality data"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getForecast(locationName: String, days: Int): Result<List<WeatherData>> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.getForecast(locationName, days)
                Result.success(response.map { it.toWeatherData() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getForecastByCoordinates(lat: Double, lon: Double, days: Int): Result<List<WeatherData>> = 
        withContext(Dispatchers.IO) {
            try {
                val response = api.getForecastByCoordinates(lat, lon, days)
                Result.success(response.map { it.toWeatherData() })
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getSavedLocations(): Result<List<Location>> = withContext(Dispatchers.IO) {
        try {
            Result.success(cache.getSavedLocations())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveLocation(location: Location): Result<Location> = withContext(Dispatchers.IO) {
        try {
            cache.saveLocation(location)
            Result.success(location)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteLocation(locationId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cache.deleteLocation(locationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun clearCache(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            cache.clear()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions
fun WeatherEntity.toWeatherData(): WeatherData {
    return WeatherData(
        temperature = temperature,
        feelsLike = feelsLike,
        description = description,
        iconCode = iconCode,
        humidity = humidity,
        windSpeed = windSpeed,
        timestamp = timestamp
    )
}

fun AirQualityEntity.toAirQuality(): AirQuality {
    return AirQuality(
        aqi = aqi,
        pm25 = pm25,
        pm10 = pm10,
        timestamp = timestamp
    )
} 
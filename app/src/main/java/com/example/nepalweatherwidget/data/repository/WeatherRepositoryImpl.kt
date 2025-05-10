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

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val airPollutionService: AirPollutionService,
    private val geocodingRepository: GeocodingRepository,
    private val networkMonitor: NetworkMonitor,
    private val weatherDao: WeatherDao,
    private val airQualityDao: AirQualityDao,
    private val errorHandler: ErrorHandler,
    @Named("openweather_api_key") private val apiKey: String,
    private val cache: WeatherCache
) : WeatherRepository {
    
    companion object {
        private const val CACHE_VALIDITY_MINUTES = 15
        private const val STALE_DATA_THRESHOLD_HOURS = 2
    }
    
    override suspend fun getWeatherDataByLocationName(locationName: String): Result<WeatherData> {
        return when (val locationResult = geocodingRepository.getLocationByName(locationName)) {
            is Result.Success -> getWeatherDataByCoordinates(locationResult.data.latitude, locationResult.data.longitude)
            is Result.Error -> locationResult
        }
    }
    
    override suspend fun getAirQualityByLocationName(locationName: String): Result<AirQuality> {
        return when (val locationResult = geocodingRepository.getLocationByName(locationName)) {
            is Result.Success -> getAirQualityByCoordinates(locationResult.data.latitude, locationResult.data.longitude)
            is Result.Error -> locationResult
        }
    }
    
    override suspend fun getWeatherAndAirQualityByLocationName(locationName: String): Result<Pair<WeatherData, AirQuality>> {
        return cache.getOrFetch(
            key = "weather_air_$locationName",
            validity = CACHE_VALIDITY_MINUTES.minutes
        ) {
            fetchWeatherAndAirQualityFromNetwork(locationName)
        }
    }
    
    override suspend fun getWeatherDataByCoordinates(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                Logger.w("WeatherRepository: Network unavailable, using cached weather data")
                val cached = weatherDao.getLatestWeatherData().first()
                if (cached != null) {
                    return Result.Success(cached.toWeatherData())
                } else {
                    return Result.Error(WeatherException.NetworkException.NoInternet)
                }
            }

            val response = withNetworkRetry {
                weatherService.getCurrentWeather(lat, lon, apiKey)
            }

            val weatherEntity = WeatherEntity.fromResponse(response)
            weatherDao.insertWeatherData(weatherEntity)

            Result.Success(weatherEntity.toWeatherData())
        } catch (e: Exception) {
            Logger.e("WeatherRepository: Error fetching weather data by coordinates", e)
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun getAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQuality> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                Logger.w("WeatherRepository: Network unavailable, using cached air quality data")
                val cached = airQualityDao.getLatestAirQualityData().first()
                if (cached != null) {
                    return Result.Success(cached.toAirQuality())
                } else {
                    return Result.Error(WeatherException.NetworkException.NoInternet)
                }
            }

            val response = withNetworkRetry {
                airPollutionService.getCurrentAirQuality(lat, lon, apiKey)
            }

            val airQualityEntity = AirQualityEntity.fromResponse(response).copy(
                location = "$lat,$lon" // Set location from coordinates
            )
            airQualityDao.insertAirQualityData(airQualityEntity)

            Result.Success(airQualityEntity.toAirQuality())
        } catch (e: Exception) {
            Logger.e("WeatherRepository: Error fetching air quality data by coordinates", e)
            errorHandler.handleError(e)
        }
    }
    
    private suspend fun fetchWeatherAndAirQualityFromNetwork(locationName: String): Result<Pair<WeatherData, AirQuality>> {
        return try {
            // First check if we have valid cached data
            val cachedWeather = weatherDao.getWeatherData(locationName).first()
            val cachedAirQuality = airQualityDao.getAirQualityData(locationName).first()
            
            if (cachedWeather != null && cachedAirQuality != null && 
                isDataValid(cachedWeather.timestamp) && isDataValid(cachedAirQuality.timestamp)) {
                return Result.Success(
                    Pair(
                        cachedWeather.toWeatherData(),
                        cachedAirQuality.toAirQuality()
                    )
                )
            }
            
            // If offline, return stale data if available
            if (!networkMonitor.isNetworkAvailable()) {
                return getCachedDataOrError(locationName)
            }
            
            // Fetch from network
            val locationResult = geocodingRepository.getLocationByName(locationName)
            when (locationResult) {
                is Result.Success -> {
                    val weatherResponse = weatherService.getCurrentWeather(
                        locationResult.data.latitude,
                        locationResult.data.longitude
                    )
                    
                    val airQualityResponse = airPollutionService.getAirPollution(
                        locationResult.data.latitude,
                        locationResult.data.longitude
                    )
                    
                    val weatherEntity = WeatherEntity.fromResponse(weatherResponse, locationName)
                    val airQualityEntity = AirQualityEntity.fromResponse(airQualityResponse, locationName)
                    
                    weatherDao.insertWeatherData(weatherEntity)
                    airQualityDao.insertAirQualityData(airQualityEntity)
                    
                    Result.Success(
                        Pair(
                            weatherEntity.toWeatherData(),
                            airQualityEntity.toAirQuality()
                        )
                    )
                }
                is Result.Error -> locationResult.map { Pair(it, AirQuality.empty()) }
            }
        } catch (e: Exception) {
            Logger.e("WeatherRepository: Error fetching weather data", e)
            getCachedDataOrError(locationName)
        }
    }
    
    private suspend fun getCachedDataOrError(locationName: String): Result<Pair<WeatherData, AirQuality>> {
        val cachedWeather = weatherDao.getWeatherData(locationName).first()
        val cachedAirQuality = airQualityDao.getAirQualityData(locationName).first()
        
        return if (cachedWeather != null && cachedAirQuality != null && 
                  isDataStale(cachedWeather.timestamp) && isDataStale(cachedAirQuality.timestamp)) {
            Result.Success(
                Pair(
                    cachedWeather.toWeatherData(),
                    cachedAirQuality.toAirQuality()
                )
            )
        } else {
            Result.Error(Exception("No internet connection and no valid cached data available"))
        }
    }
    
    private fun isDataValid(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age < TimeUnit.MINUTES.toMillis(CACHE_VALIDITY_MINUTES.toLong())
    }
    
    private fun isDataStale(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age < TimeUnit.HOURS.toMillis(STALE_DATA_THRESHOLD_HOURS.toLong())
    }
    
    // Legacy methods implementation
    override fun getWeatherData(location: String): Flow<Result<WeatherData>> = flow {
        emit(getWeatherDataByLocationName(location))
    }
    
    override fun getAirQuality(location: String): Flow<Result<AirQuality>> = flow {
        emit(getAirQualityByLocationName(location))
    }
    
    override suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>> {
        return getWeatherAndAirQualityByLocationName(location)
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
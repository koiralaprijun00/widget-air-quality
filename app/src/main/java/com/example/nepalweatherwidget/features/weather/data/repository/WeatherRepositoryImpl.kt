package com.example.nepalweatherwidget.features.weather.data.repository

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
import com.example.nepalweatherwidget.domain.model.Location
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.GeocodingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes
import com.example.nepalweatherwidget.core.di.qualifiers.OpenWeatherApiKey
import com.example.nepalweatherwidget.core.di.qualifiers.WeatherApiService
import com.example.nepalweatherwidget.core.di.qualifiers.AirQualityApiService
import com.example.nepalweatherwidget.features.weather.data.datasource.local.WeatherLocalDataSource
import com.example.nepalweatherwidget.features.weather.data.datasource.remote.WeatherRemoteDataSource
import com.example.nepalweatherwidget.features.weather.data.mapper.WeatherMapper

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    @WeatherApiService private val weatherService: WeatherService,
    @AirQualityApiService private val airPollutionService: AirPollutionService,
    private val weatherDao: WeatherDao,
    private val airQualityDao: AirQualityDao,
    private val geocodingRepository: GeocodingRepository,
    private val networkMonitor: NetworkMonitor,
    private val errorHandler: ErrorHandler,
    private val weatherCache: WeatherCache,
    @OpenWeatherApiKey private val apiKey: String,
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource,
    private val weatherMapper: WeatherMapper
) : WeatherRepository {
    
    companion object {
        private const val TAG = "WeatherRepository"
        private val CACHE_VALIDITY = 30.minutes
        private const val CACHE_STALE_THRESHOLD_HOURS = 2L
    }
    
    override suspend fun getWeatherData(locationName: String): Result<WeatherData> {
        return try {
            // Try remote first
            val remoteResult = remoteDataSource.getWeatherData(locationName)
            
            if (remoteResult is Result.Success) {
                // Cache the data
                localDataSource.saveWeatherData(remoteResult.data)
                Result.Success(weatherMapper.toDomain(remoteResult.data))
            } else {
                // Fallback to cache
                val cachedData = localDataSource.getWeatherData(locationName)
                if (cachedData != null) {
                    Result.Success(weatherMapper.toDomain(cachedData))
                } else {
                    remoteResult
                }
            }
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }
    
    override suspend fun getAirQuality(locationName: String): Result<AirQuality> {
        return weatherCache.getOrFetch(
            key = "air_quality_$locationName",
            validity = CACHE_VALIDITY,
            fetcher = { fetchAirQuality(locationName) }
        )
    }
    
    override suspend fun getWeatherAndAirQuality(locationName: String): Result<Pair<WeatherData, AirQuality>> {
        return try {
            val weatherResult = getWeatherData(locationName)
            val airQualityResult = getAirQuality(locationName)
            
            when {
                weatherResult is Result.Success && airQualityResult is Result.Success -> {
                    Result.Success(Pair(weatherResult.data, airQualityResult.data))
                }
                weatherResult is Result.Error -> weatherResult
                airQualityResult is Result.Error -> airQualityResult
                else -> Result.Error(WeatherException.UnknownError())
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun getWeatherDataByCoordinates(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            val remoteResult = remoteDataSource.getWeatherByCoordinates(lat, lon)
            
            if (remoteResult is Result.Success) {
                localDataSource.saveWeatherData(remoteResult.data)
                Result.Success(weatherMapper.toDomain(remoteResult.data))
            } else {
                remoteResult
            }
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }
    
    override suspend fun getAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQuality> {
        return weatherCache.getOrFetch(
            key = "air_quality_coords_${lat}_${lon}",
            validity = CACHE_VALIDITY,
            fetcher = { fetchAirQualityByCoordinates(lat, lon) }
        )
    }
    
    override suspend fun getWeatherAndAirQualityByCoordinates(lat: Double, lon: Double): Result<Pair<WeatherData, AirQuality>> {
        return try {
            val weatherResult = getWeatherDataByCoordinates(lat, lon)
            val airQualityResult = getAirQualityByCoordinates(lat, lon)
            
            when {
                weatherResult is Result.Success && airQualityResult is Result.Success -> {
                    Result.Success(Pair(weatherResult.data, airQualityResult.data))
                }
                weatherResult is Result.Error -> weatherResult
                airQualityResult is Result.Error -> airQualityResult
                else -> Result.Error(WeatherException.UnknownError())
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun getForecast(locationName: String, days: Int): Result<List<WeatherData>> {
        return try {
            val remoteResult = remoteDataSource.getForecast(locationName)
            
            if (remoteResult is Result.Success) {
                Result.Success(weatherMapper.toDomain(remoteResult.data))
            } else {
                remoteResult
            }
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }
    
    override suspend fun getForecastByCoordinates(lat: Double, lon: Double, days: Int): Result<List<WeatherData>> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                return Result.Error(WeatherException.NetworkException.NoInternet)
            }

            val response = withNetworkRetry {
                weatherService.getForecast(lat, lon, apiKey)
            }

            val forecastData = response.list.map { forecastItem ->
                WeatherData(
                    temperature = forecastItem.main.temp,
                    feelsLike = forecastItem.main.feelsLike,
                    description = forecastItem.weather.firstOrNull()?.description ?: "",
                    iconCode = forecastItem.weather.firstOrNull()?.icon ?: "",
                    humidity = forecastItem.main.humidity,
                    windSpeed = forecastItem.wind.speed,
                    timestamp = forecastItem.dt * 1000 // Convert to milliseconds
                )
            }

            Result.Success(forecastData)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun getSavedLocations(): Result<List<Location>> {
        return try {
            val locations = weatherDao.getSavedLocations().first()
            Result.Success(locations.map { it.toLocation() })
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun saveLocation(location: Location): Result<Location> {
        return try {
            val locationEntity = LocationEntity(
                id = location.id,
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis()
            )
            weatherDao.insertLocation(locationEntity)
            Result.Success(location)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun deleteLocation(locationId: String): Result<Unit> {
        return try {
            weatherDao.deleteLocation(locationId)
            Result.Success(Unit)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun clearCache(): Result<Unit> {
        return try {
            localDataSource.clearCache()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }
    
    // Private helper methods
    private suspend fun fetchAirQuality(locationName: String): Result<AirQuality> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                return tryGetCachedAirQuality(locationName) ?: Result.Error(WeatherException.NetworkException.NoInternet)
            }
            
            // Get coordinates for location
            val locationResult = geocodingRepository.getLocationByName(locationName)
            return when (locationResult) {
                is Result.Success -> {
                    val location = locationResult.data
                    fetchAirQualityByCoordinates(location.latitude, location.longitude)
                }
                is Result.Error -> locationResult
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    private suspend fun fetchAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQuality> {
        return try {
            val response = withNetworkRetry {
                airPollutionService.getCurrentAirQuality(lat, lon, apiKey)
            }
            
            val components = response.list.firstOrNull()?.components
            val main = response.list.firstOrNull()?.main
            
            if (components == null || main == null) {
                return Result.Error(WeatherException.DataException.InvalidData())
            }
            
            val airQuality = AirQuality(
                aqi = main.aqi,
                pm25 = components.pm25,
                pm10 = components.pm10,
                timestamp = System.currentTimeMillis()
            )
            
            // Save to local database
            saveAirQualityToLocal("", airQuality) // TODO: Get location name
            
            Result.Success(airQuality)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    private suspend fun saveAirQualityToLocal(location: String, airQuality: AirQuality) {
        try {
            val entity = AirQualityEntity(
                location = location,
                aqi = airQuality.aqi,
                pm25 = airQuality.pm25,
                pm10 = airQuality.pm10,
                timestamp = airQuality.timestamp
            )
            airQualityDao.insertAirQualityData(entity)
        } catch (e: Exception) {
            Logger.e("Failed to save air quality data to local database", e)
        }
    }
    
    private suspend fun tryGetCachedAirQuality(location: String): Result<AirQuality>? {
        return try {
            val cachedEntity = airQualityDao.getAirQualityData(location).first()
            if (cachedEntity != null && isCachedDataValid(cachedEntity.timestamp)) {
                Result.Success(cachedEntity.toAirQuality())
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e("Failed to get cached air quality data", e)
            null
        }
    }
    
    private fun isCachedDataValid(timestamp: Long): Boolean {
        val age = System.currentTimeMillis() - timestamp
        return age < TimeUnit.HOURS.toMillis(CACHE_STALE_THRESHOLD_HOURS)
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
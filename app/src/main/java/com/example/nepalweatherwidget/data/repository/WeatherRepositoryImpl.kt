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
import com.example.nepalweatherwidget.domain.model.Location
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.GeocodingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val airPollutionService: AirPollutionService,
    private val weatherDao: WeatherDao,
    private val airQualityDao: AirQualityDao,
    private val geocodingRepository: GeocodingRepository,
    private val networkMonitor: NetworkMonitor,
    private val errorHandler: ErrorHandler,
    private val weatherCache: WeatherCache,
    @Named("openweather_api_key") private val apiKey: String
) : WeatherRepository {
    
    companion object {
        private const val TAG = "WeatherRepository"
        private val CACHE_VALIDITY = 30.minutes
        private const val CACHE_STALE_THRESHOLD_HOURS = 2L
    }
    
    override suspend fun getWeatherData(locationName: String): Result<WeatherData> {
        return weatherCache.getOrFetch(
            key = "weather_$locationName",
            validity = CACHE_VALIDITY,
            fetcher = { fetchWeatherData(locationName) }
        )
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
        return weatherCache.getOrFetch(
            key = "weather_coords_${lat}_${lon}",
            validity = CACHE_VALIDITY,
            fetcher = { fetchWeatherDataByCoordinates(lat, lon) }
        )
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
        // TODO: Implement forecast fetching
        return Result.Error(WeatherException.DataException.NoDataAvailable)
    }
    
    override suspend fun getForecastByCoordinates(lat: Double, lon: Double, days: Int): Result<List<WeatherData>> {
        // TODO: Implement forecast fetching by coordinates
        return Result.Error(WeatherException.DataException.NoDataAvailable)
    }
    
    override suspend fun getSavedLocations(): Result<List<Location>> {
        // TODO: Implement saved locations
        return Result.Success(emptyList())
    }
    
    override suspend fun saveLocation(location: Location): Result<Location> {
        // TODO: Implement location saving
        return Result.Success(location)
    }
    
    override suspend fun deleteLocation(locationId: String): Result<Unit> {
        // TODO: Implement location deletion
        return Result.Success(Unit)
    }
    
    override suspend fun clearCache(): Result<Unit> {
        return try {
            weatherCache.invalidateAll()
            Result.Success(Unit)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    // Private helper methods
    private suspend fun fetchWeatherData(locationName: String): Result<WeatherData> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                return tryGetCachedData(locationName) ?: Result.Error(WeatherException.NetworkException.NoInternet)
            }
            
            // Get coordinates for location
            val locationResult = geocodingRepository.getLocationByName(locationName)
            return when (locationResult) {
                is Result.Success -> {
                    val location = locationResult.data
                    fetchWeatherDataByCoordinates(location.latitude, location.longitude)
                }
                is Result.Error -> locationResult
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    private suspend fun fetchWeatherDataByCoordinates(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            val response = withNetworkRetry {
                weatherService.getCurrentWeather(lat, lon, apiKey)
            }
            
            val weatherData = WeatherData(
                temperature = response.main.temp,
                feelsLike = response.main.feelsLike,
                description = response.weather.firstOrNull()?.description ?: "",
                iconCode = response.weather.firstOrNull()?.icon ?: "",
                humidity = response.main.humidity,
                windSpeed = response.wind.speed,
                timestamp = response.timestamp * 1000 // Convert to milliseconds
            )
            
            // Save to local database
            saveWeatherDataToLocal(response.name, weatherData)
            
            Result.Success(weatherData)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
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
    
    private suspend fun saveWeatherDataToLocal(location: String, weatherData: WeatherData) {
        try {
            val entity = WeatherEntity(
                location = location,
                temperature = weatherData.temperature,
                feelsLike = weatherData.feelsLike,
                description = weatherData.description,
                iconCode = weatherData.iconCode,
                humidity = weatherData.humidity,
                windSpeed = weatherData.windSpeed,
                timestamp = weatherData.timestamp
            )
            weatherDao.insertWeatherData(entity)
        } catch (e: Exception) {
            Logger.e("Failed to save weather data to local database", e)
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
    
    private suspend fun tryGetCachedData(location: String): Result<WeatherData>? {
        return try {
            val cachedEntity = weatherDao.getWeatherData(location).first()
            if (cachedEntity != null && isCachedDataValid(cachedEntity.timestamp)) {
                Result.Success(cachedEntity.toWeatherData())
            } else {
                null
            }
        } catch (e: Exception) {
            Logger.e("Failed to get cached weather data", e)
            null
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
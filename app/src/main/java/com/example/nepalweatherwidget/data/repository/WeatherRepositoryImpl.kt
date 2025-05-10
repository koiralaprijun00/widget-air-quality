package com.example.nepalweatherwidget.data.repository

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
    @Named("openweather_api_key") private val apiKey: String
) : WeatherRepository {
    
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
        return when (val locationResult = geocodingRepository.getLocationByName(locationName)) {
            is Result.Success -> getWeatherAndAirQualityByCoordinates(locationResult.data.latitude, locationResult.data.longitude)
            is Result.Error -> locationResult
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
    
    override suspend fun getWeatherAndAirQualityByCoordinates(lat: Double, lon: Double): Result<Pair<WeatherData, AirQuality>> {
        return try {
            val weatherResult = getWeatherDataByCoordinates(lat, lon)
            val airQualityResult = getAirQualityByCoordinates(lat, lon)

            when {
                weatherResult is Result.Success && airQualityResult is Result.Success -> {
                    Result.Success(weatherResult.data to airQualityResult.data)
                }
                weatherResult is Result.Error -> weatherResult
                airQualityResult is Result.Error -> airQualityResult
                else -> Result.Error(WeatherException.UnknownError("Unknown error occurred"))
            }
        } catch (e: Exception) {
            Logger.e("WeatherRepository: Error fetching weather and air quality data by coordinates", e)
            errorHandler.handleError(e)
        }
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
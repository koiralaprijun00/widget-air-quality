package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.extension.withNetworkRetry
import com.example.nepalweatherwidget.core.network.NetworkMonitor
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.remote.api.WeatherService
import com.example.nepalweatherwidget.data.remote.mapper.toAirQuality
import com.example.nepalweatherwidget.data.remote.mapper.toEntity
import com.example.nepalweatherwidget.data.remote.mapper.toWeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeout
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val airPollutionService: AirPollutionService,
    private val networkMonitor: NetworkMonitor,
    private val weatherDao: WeatherDao,
    private val airQualityDao: AirQualityDao,
    private val errorHandler: ErrorHandler,
    @Named("openweather_api_key") private val apiKey: String
) : WeatherRepository {
    
    override suspend fun getWeatherData(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                // Try to get from cache
                val cached = weatherDao.getLatestWeatherData().first()
                if (cached != null) {
                    Result.Success(cached.toWeatherData())
                } else {
                    Result.Error(WeatherException.NetworkException.NoInternet)
                }
            } else {
                fetchWeatherDataFromApi(lat, lon)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    private suspend fun fetchWeatherDataFromApi(lat: Double, lon: Double): Result<WeatherData> {
        return withNetworkRetry {
            withTimeout(10_000) {
                val response = weatherService.getCurrentWeather(lat, lon, apiKey)
                val weatherData = response.toWeatherData()
                
                // Cache the data
                weatherDao.insertWeatherData(weatherData.toEntity(location = "$lat,$lon"))
                
                Result.Success(weatherData)
            }
        }
    }
    
    override suspend fun getAirQuality(lat: Double, lon: Double): Result<AirQuality> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                // Try to get from cache
                val cached = airQualityDao.getLatestAirQuality().first()
                if (cached != null) {
                    Result.Success(cached.toAirQuality())
                } else {
                    Result.Error(WeatherException.NetworkException.NoInternet)
                }
            } else {
                fetchAirQualityFromApi(lat, lon)
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    private suspend fun fetchAirQualityFromApi(lat: Double, lon: Double): Result<AirQuality> {
        return withNetworkRetry {
            withTimeout(10_000) {
                val response = airPollutionService.getCurrentAirQuality(lat, lon, apiKey)
                val airQuality = response.list.firstOrNull()?.toAirQuality()
                    ?: return@withTimeout Result.Error(WeatherException.DataException.NoDataAvailable)
                
                // Cache the data
                airQualityDao.insertAirQuality(airQuality.toEntity(location = "$lat,$lon"))
                
                Result.Success(airQuality)
            }
        }
    }
    
    override suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>> {
        return try {
            // For simplicity, using Kathmandu coordinates
            val lat = 27.7172
            val lon = 85.3240
            
            val weatherResult = getWeatherData(lat, lon)
            val airQualityResult = getAirQuality(lat, lon)
            
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
} 
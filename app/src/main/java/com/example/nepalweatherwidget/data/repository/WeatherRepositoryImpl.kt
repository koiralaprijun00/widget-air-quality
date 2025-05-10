package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.core.util.NetworkException
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.remote.api.WeatherService
import com.example.nepalweatherwidget.data.remote.mapper.AirQualityMapper.toAirQuality
import com.example.nepalweatherwidget.data.remote.mapper.WeatherMapper.toWeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.network.NetworkMonitor
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.firstOrNull
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
    @Named("openweather_api_key") private val apiKey: String
) : WeatherRepository {
    
    override suspend fun getWeatherData(lat: Double, lon: Double): Result<WeatherData> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                // Try to get from cache
                val cached = weatherDao.getLatestWeatherData().firstOrNull()
                if (cached != null) {
                    return Result.success(cached.toWeatherData())
                }
                return Result.failure(NetworkException("No internet connection"))
            }
            
            val response = weatherService.getCurrentWeather(lat, lon, apiKey)
            val weatherData = response.toWeatherData()
            
            // Cache the data
            weatherDao.insertWeatherData(weatherData.toEntity(location = "$lat,$lon"))
            
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getAirQuality(lat: Double, lon: Double): Result<AirQuality> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                // Try to get from cache
                val cached = airQualityDao.getLatestAirQuality().firstOrNull()
                if (cached != null) {
                    return Result.success(cached.toAirQuality())
                }
                return Result.failure(NetworkException("No internet connection"))
            }
            
            val response = airPollutionService.getCurrentAirQuality(lat, lon, apiKey)
            val airQuality = response.list.firstOrNull()?.toAirQuality()
                ?: return Result.failure(Exception("No air quality data available"))
            
            // Cache the data
            airQualityDao.insertAirQuality(airQuality.toEntity(location = "$lat,$lon"))
            
            Result.success(airQuality)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>> {
        return try {
            // For simplicity, we'll use Kathmandu coordinates
            val lat = 27.7172
            val lon = 85.3240
            
            val weatherResult = getWeatherData(lat, lon)
            val airQualityResult = getAirQuality(lat, lon)
            
            if (weatherResult.isSuccess && airQualityResult.isSuccess) {
                Result.success(Pair(weatherResult.getOrNull()!!, airQualityResult.getOrNull()!!))
            } else {
                Result.failure(Exception("Failed to get weather or air quality data"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
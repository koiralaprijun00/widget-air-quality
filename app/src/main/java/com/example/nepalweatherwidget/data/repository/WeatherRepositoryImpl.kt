package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.data.api.WeatherApiService
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val apiService: WeatherApiService
) : WeatherRepository {
    private var cachedWeather: WeatherData? = null
    private var cachedAirQuality: AirQuality? = null

    override suspend fun getWeatherData(location: String): Result<WeatherData> {
        return try {
            // For Nepal, using Kathmandu coordinates
            val response = apiService.getAirPollution(
                latitude = 27.7172,
                longitude = 85.3240,
                apiKey = "YOUR_API_KEY" // Replace with your actual API key
            )
            
            val airData = response.list.first()
            val weatherData = WeatherData(
                temperature = 0.0, // Temperature not available in air pollution API
                humidity = 0, // Humidity not available in air pollution API
                description = getAirQualityDescription(airData.main.aqi),
                location = location,
                timestamp = airData.dt * 1000 // Convert to milliseconds
            )
            
            cachedWeather = weatherData
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCachedWeatherData(): Result<WeatherData> {
        return cachedWeather?.let { Result.success(it) }
            ?: Result.failure(Exception("No cached weather data available"))
    }

    override suspend fun getAirQuality(): Result<AirQuality> {
        return try {
            // For Nepal, using Kathmandu coordinates
            val response = apiService.getAirPollution(
                latitude = 27.7172,
                longitude = 85.3240,
                apiKey = "YOUR_API_KEY" // Replace with your actual API key
            )
            
            val airData = response.list.first()
            val airQuality = AirQuality(
                aqi = airData.main.aqi,
                pm25 = airData.components.pm2_5,
                pm10 = airData.components.pm10,
                timestamp = airData.dt * 1000 // Convert to milliseconds
            )
            
            cachedAirQuality = airQuality
            Result.success(airQuality)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getAirQualityDescription(aqi: Int): String {
        return when (aqi) {
            1 -> "Good"
            2 -> "Fair"
            3 -> "Moderate"
            4 -> "Poor"
            5 -> "Very Poor"
            else -> "Unknown"
        }
    }
} 
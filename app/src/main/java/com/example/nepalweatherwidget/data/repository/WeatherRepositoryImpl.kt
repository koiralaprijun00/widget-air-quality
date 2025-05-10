package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.data.api.WeatherService
import com.example.nepalweatherwidget.data.api.AirPollutionService
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.R
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val airPollutionService: AirPollutionService
) : WeatherRepository {
    
    override suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>> {
        return try {
            // For now, use hardcoded coordinates for Kathmandu
            // In a real app, you would geocode the location string
            val lat = 27.7172
            val lon = 85.3240
            
            val weatherResponse = weatherService.getCurrentWeather(lat, lon)
            val airQualityResponse = airPollutionService.getCurrentAirQuality(lat, lon)
            
            val weatherData = WeatherData(
                temperature = weatherResponse.main.temp,
                humidity = weatherResponse.main.humidity,
                description = weatherResponse.weather.firstOrNull()?.description ?: "",
                location = weatherResponse.name,
                windSpeed = weatherResponse.wind.speed
            )
            
            val airQualityData = airQualityResponse.list.firstOrNull()?.let { aqData ->
                AirQuality(
                    aqi = aqData.main.aqi,
                    pm25 = aqData.components.pm2_5,
                    pm10 = aqData.components.pm10
                )
            } ?: throw Exception("No air quality data available")
            
            Result.success(Pair(weatherData, airQualityData))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.data.api.WeatherService
import com.example.nepalweatherwidget.data.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService
) : WeatherRepository {

    override suspend fun getCurrentWeather(lat: Double, lon: Double): WeatherData {
        return try {
            val response = weatherService.getCurrentWeather(lat, lon)
            WeatherData(
                temperature = response.main.temp,
                humidity = response.main.humidity,
                description = response.weather.firstOrNull()?.description ?: "",
                location = response.name,
                windSpeed = response.wind.speed
            )
        } catch (e: Exception) {
            throw Exception("Failed to fetch weather data: ${e.message}")
        }
    }
} 
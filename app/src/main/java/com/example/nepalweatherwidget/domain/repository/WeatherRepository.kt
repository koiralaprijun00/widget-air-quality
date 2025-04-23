package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData

interface WeatherRepository {
    suspend fun getWeatherData(location: String): Result<WeatherData>
    suspend fun getCachedWeatherData(): Result<WeatherData>
    suspend fun getAirQuality(): Result<AirQuality>
} 
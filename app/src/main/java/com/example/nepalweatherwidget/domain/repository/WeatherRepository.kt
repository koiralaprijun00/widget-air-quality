package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData

interface WeatherRepository {
    suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>>
    suspend fun getWeatherData(lat: Double, lon: Double): Result<WeatherData>
    suspend fun getAirQuality(lat: Double, lon: Double): Result<AirQuality>
} 
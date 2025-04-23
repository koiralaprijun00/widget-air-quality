package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData

interface WeatherRepository {
    suspend fun getWeatherData(location: String): Result<WeatherData>
    suspend fun getWeatherData(latitude: Double, longitude: Double): Result<WeatherData>
    suspend fun getCachedWeatherData(): Result<WeatherData>
    suspend fun getAirQuality(): Result<AirQuality>
    suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQuality>
    suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>>
    suspend fun getWeatherAndAirQuality(latitude: Double, longitude: Double): Result<Pair<WeatherData, AirQuality>>
} 
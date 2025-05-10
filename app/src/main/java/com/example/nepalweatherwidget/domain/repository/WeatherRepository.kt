package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.ApiResult
import com.example.nepalweatherwidget.domain.model.WeatherData

interface WeatherRepository {
    suspend fun getWeatherAndAirQuality(location: String): ApiResult<Pair<WeatherData, AirQuality>>
    suspend fun getWeatherData(lat: Double, lon: Double): ApiResult<WeatherData>
    suspend fun getAirQuality(lat: Double, lon: Double): ApiResult<AirQuality>
} 
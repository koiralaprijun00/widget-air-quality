package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQualityData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    fun getWeatherData(location: String): Flow<Result<WeatherData>>
    fun getAirQuality(location: String): Flow<Result<AirQualityData>>
    suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQualityData>>
} 
package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    // Location name based methods
    suspend fun getWeatherDataByLocationName(locationName: String): Result<WeatherData>
    suspend fun getAirQualityByLocationName(locationName: String): Result<AirQuality>
    suspend fun getWeatherAndAirQualityByLocationName(locationName: String): Result<Pair<WeatherData, AirQuality>>
    
    // Coordinate based methods
    suspend fun getWeatherDataByCoordinates(lat: Double, lon: Double): Result<WeatherData>
    suspend fun getAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQuality>
    suspend fun getWeatherAndAirQualityByCoordinates(lat: Double, lon: Double): Result<Pair<WeatherData, AirQuality>>
    
    // Legacy methods (to be deprecated)
    fun getWeatherData(location: String): Flow<Result<WeatherData>>
    fun getAirQuality(location: String): Flow<Result<AirQuality>>
    suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>>
} 
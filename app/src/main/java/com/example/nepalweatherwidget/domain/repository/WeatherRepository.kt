package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQualityData
import kotlinx.coroutines.flow.Flow

interface WeatherRepository {
    // Location name based methods
    suspend fun getWeatherDataByLocationName(locationName: String): Result<WeatherData>
    suspend fun getAirQualityByLocationName(locationName: String): Result<AirQualityData>
    suspend fun getWeatherAndAirQualityByLocationName(locationName: String): Result<Pair<WeatherData, AirQualityData>>
    
    // Coordinate based methods
    suspend fun getWeatherDataByCoordinates(lat: Double, lon: Double): Result<WeatherData>
    suspend fun getAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQualityData>
    suspend fun getWeatherAndAirQualityByCoordinates(lat: Double, lon: Double): Result<Pair<WeatherData, AirQualityData>>
    
    // Legacy methods (to be deprecated)
    fun getWeatherData(location: String): Flow<Result<WeatherData>>
    fun getAirQuality(location: String): Flow<Result<AirQualityData>>
    suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQualityData>>
} 
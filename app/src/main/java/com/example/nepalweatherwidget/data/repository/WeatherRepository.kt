package com.example.nepalweatherwidget.data.repository

import android.util.Log
import com.example.nepalweatherwidget.data.api.AirQualityApi
import com.example.nepalweatherwidget.data.api.WeatherApi
import com.example.nepalweatherwidget.data.model.AirQualityData
import com.example.nepalweatherwidget.data.model.WeatherData
import com.example.nepalweatherwidget.data.model.toAirQualityData
import com.example.nepalweatherwidget.data.model.toWeatherData
import com.example.nepalweatherwidget.data.util.Result
import com.example.nepalweatherwidget.data.util.Result.Companion.safeApiCall
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData as DomainWeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository as DomainWeatherRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val airQualityApi: AirQualityApi,
    private val apiKey: String
) : DomainWeatherRepository {
    private val TAG = "WeatherRepository"
    private var cachedWeather: DomainWeatherData? = null
    private var cachedAirQuality: AirQuality? = null

    override suspend fun getWeatherData(location: String): Result<DomainWeatherData> =
        safeApiCall {
            Log.d(TAG, "Fetching weather data for location: $location")
            val (latitude, longitude) = getCoordinatesForLocation(location)
            Log.d(TAG, "Using coordinates: lat=$latitude, lon=$longitude")
            
            val weather = weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toWeatherData()
            
            Log.d(TAG, "Weather data received: $weather")
            cachedWeather = weather.toDomainModel()
            weather.toDomainModel()
        }

    override suspend fun getCachedWeatherData(): Result<DomainWeatherData> {
        Log.d(TAG, "Getting cached weather data")
        return cachedWeather?.let { 
            Log.d(TAG, "Cached weather data found")
            Result.success(it) 
        } ?: run {
            Log.d(TAG, "No cached weather data available")
            Result.failure(Exception("No cached weather data available"))
        }
    }

    override suspend fun getAirQuality(): Result<AirQuality> =
        safeApiCall {
            Log.d(TAG, "Fetching air quality data")
            val (latitude, longitude) = getCoordinatesForLocation("Kathmandu")
            Log.d(TAG, "Using coordinates: lat=$latitude, lon=$longitude")
            
            val airQuality = airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toAirQualityData()
            
            Log.d(TAG, "Air quality data received: $airQuality")
            cachedAirQuality = airQuality.toDomainModel()
            airQuality.toDomainModel()
        }

    override suspend fun getWeatherAndAirQuality(location: String): Result<Pair<DomainWeatherData, AirQuality>> =
        safeApiCall {
            Log.d(TAG, "Fetching weather and air quality data for location: $location")
            val (latitude, longitude) = getCoordinatesForLocation(location)
            Log.d(TAG, "Using coordinates: lat=$latitude, lon=$longitude")
            
            val weather = weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toWeatherData().toDomainModel()
            
            Log.d(TAG, "Weather data received: $weather")
            
            val airQuality = airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toAirQualityData().toDomainModel()
            
            Log.d(TAG, "Air quality data received: $airQuality")
            
            cachedWeather = weather
            cachedAirQuality = airQuality
            
            Pair(weather, airQuality)
        }

    private fun getCoordinatesForLocation(location: String): Pair<Double, Double> {
        // For now, hardcoding Kathmandu coordinates
        // TODO: Implement proper location lookup
        Log.d(TAG, "Using fallback coordinates for Kathmandu")
        return Pair(27.7172, 85.3240)
    }

    private fun WeatherData.toDomainModel(): DomainWeatherData =
        DomainWeatherData(
            temperature = temperature,
            humidity = humidity,
            description = description,
            location = location,
            timestamp = timestamp
        )

    private fun AirQualityData.toDomainModel(): AirQuality =
        AirQuality(
            aqi = aqi,
            pm25 = pm25,
            pm10 = pm10,
            timestamp = System.currentTimeMillis()
        )
} 
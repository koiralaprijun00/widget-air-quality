package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.data.api.AirQualityApi
import com.example.nepalweatherwidget.data.api.WeatherApi
import com.example.nepalweatherwidget.data.model.AirQualityData
import com.example.nepalweatherwidget.data.model.WeatherData
import com.example.nepalweatherwidget.data.model.toAirQualityData
import com.example.nepalweatherwidget.data.model.toWeatherData
import com.example.nepalweatherwidget.data.util.Result
import com.example.nepalweatherwidget.data.util.Result.Companion.safeApiCall
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherRepository @Inject constructor(
    private val weatherApi: WeatherApi,
    private val airQualityApi: AirQualityApi,
    private val apiKey: String
) {
    suspend fun getWeatherData(latitude: Double, longitude: Double): Result<WeatherData> =
        safeApiCall {
            weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toWeatherData()
        }

    suspend fun getAirQualityData(latitude: Double, longitude: Double): Result<AirQualityData> =
        safeApiCall {
            airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toAirQualityData()
        }

    suspend fun getWeatherAndAirQuality(
        latitude: Double,
        longitude: Double
    ): Result<Pair<WeatherData, AirQualityData>> = safeApiCall {
        val weather = weatherApi.getWeather(latitude, longitude, apiKey).toWeatherData()
        val airQuality = airQualityApi.getAirQuality(latitude, longitude, apiKey).toAirQualityData()
        Pair(weather, airQuality)
    }
} 
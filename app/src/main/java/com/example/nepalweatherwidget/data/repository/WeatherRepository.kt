package com.example.nepalweatherwidget.data.repository

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
    private var cachedWeather: DomainWeatherData? = null
    private var cachedAirQuality: AirQuality? = null

    override suspend fun getWeatherData(location: String): Result<DomainWeatherData> =
        safeApiCall {
            // For Nepal, using Kathmandu coordinates as fallback
            val (latitude, longitude) = getCoordinatesForLocation(location)
            val weather = weatherApi.getWeather(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toWeatherData()
            cachedWeather = weather.toDomainModel()
            weather.toDomainModel()
        }

    override suspend fun getCachedWeatherData(): Result<DomainWeatherData> =
        cachedWeather?.let { Result.success(it) }
            ?: Result.failure(Exception("No cached weather data available"))

    override suspend fun getAirQuality(): Result<AirQuality> =
        safeApiCall {
            // For Nepal, using Kathmandu coordinates as fallback
            val (latitude, longitude) = getCoordinatesForLocation("Kathmandu")
            val airQuality = airQualityApi.getAirQuality(
                latitude = latitude,
                longitude = longitude,
                apiKey = apiKey
            ).toAirQualityData()
            cachedAirQuality = airQuality.toDomainModel()
            airQuality.toDomainModel()
        }

    private fun getCoordinatesForLocation(location: String): Pair<Double, Double> {
        // For now, hardcoding Kathmandu coordinates
        // TODO: Implement proper location lookup
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
            components = components,
            timestamp = timestamp
        )
} 
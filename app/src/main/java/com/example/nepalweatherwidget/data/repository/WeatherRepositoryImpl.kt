package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.data.api.WeatherService
import com.example.nepalweatherwidget.data.api.AirPollutionService
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val airPollutionService: AirPollutionService
) : WeatherRepository {
    
    override suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQuality>> {
        return try {
            // Add proper geocoding here
            val lat = 27.7172
            val lon = 85.3240
            
            val weatherResponse = weatherService.getCurrentWeather(lat, lon)
            val airQualityResponse = airPollutionService.getCurrentAirQuality(lat, lon)
            
            val weatherData = WeatherData(
                temperature = weatherResponse.main.temp,
                humidity = weatherResponse.main.humidity,
                description = weatherResponse.weather.firstOrNull()?.description ?: "",
                location = weatherResponse.name,
                windSpeed = weatherResponse.wind.speed
            )
            
            val airQuality = AirQuality(
                aqi = airQualityResponse.list.firstOrNull()?.main?.aqi ?: 0,
                pm25 = airQualityResponse.list.firstOrNull()?.components?.pm2_5 ?: 0.0,
                pm10 = airQualityResponse.list.firstOrNull()?.components?.pm10 ?: 0.0
            )
            
            Result.success(Pair(weatherData, airQuality))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWeatherData(location: String): Result<WeatherData> {
        return try {
            val lat = 27.7172
            val lon = 85.3240
            
            val weatherResponse = weatherService.getCurrentWeather(lat, lon)
            val weatherData = WeatherData(
                temperature = weatherResponse.main.temp,
                humidity = weatherResponse.main.humidity,
                description = weatherResponse.weather.firstOrNull()?.description ?: "",
                location = weatherResponse.name,
                windSpeed = weatherResponse.wind.speed
            )
            
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWeatherData(latitude: Double, longitude: Double): Result<WeatherData> {
        return try {
            val weatherResponse = weatherService.getCurrentWeather(latitude, longitude)
            val weatherData = WeatherData(
                temperature = weatherResponse.main.temp,
                humidity = weatherResponse.main.humidity,
                description = weatherResponse.weather.firstOrNull()?.description ?: "",
                location = weatherResponse.name,
                windSpeed = weatherResponse.wind.speed
            )
            
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAirQuality(): Result<AirQuality> {
        return try {
            val lat = 27.7172
            val lon = 85.3240
            
            val airQualityResponse = airPollutionService.getCurrentAirQuality(lat, lon)
            val airQuality = AirQuality(
                aqi = airQualityResponse.list.firstOrNull()?.main?.aqi ?: 0,
                pm25 = airQualityResponse.list.firstOrNull()?.components?.pm2_5 ?: 0.0,
                pm10 = airQualityResponse.list.firstOrNull()?.components?.pm10 ?: 0.0
            )
            
            Result.success(airQuality)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQuality> {
        return try {
            val airQualityResponse = airPollutionService.getCurrentAirQuality(latitude, longitude)
            val airQuality = AirQuality(
                aqi = airQualityResponse.list.firstOrNull()?.main?.aqi ?: 0,
                pm25 = airQualityResponse.list.firstOrNull()?.components?.pm2_5 ?: 0.0,
                pm10 = airQualityResponse.list.firstOrNull()?.components?.pm10 ?: 0.0
            )
            
            Result.success(airQuality)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getWeatherAndAirQuality(latitude: Double, longitude: Double): Result<Pair<WeatherData, AirQuality>> {
        return try {
            val weatherResponse = weatherService.getCurrentWeather(latitude, longitude)
            val airQualityResponse = airPollutionService.getCurrentAirQuality(latitude, longitude)
            
            val weatherData = WeatherData(
                temperature = weatherResponse.main.temp,
                humidity = weatherResponse.main.humidity,
                description = weatherResponse.weather.firstOrNull()?.description ?: "",
                location = weatherResponse.name,
                windSpeed = weatherResponse.wind.speed
            )
            
            val airQuality = AirQuality(
                aqi = airQualityResponse.list.firstOrNull()?.main?.aqi ?: 0,
                pm25 = airQualityResponse.list.firstOrNull()?.components?.pm2_5 ?: 0.0,
                pm10 = airQualityResponse.list.firstOrNull()?.components?.pm10 ?: 0.0
            )
            
            Result.success(Pair(weatherData, airQuality))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
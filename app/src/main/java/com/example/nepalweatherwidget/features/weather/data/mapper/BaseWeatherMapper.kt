package com.example.nepalweatherwidget.features.weather.data.mapper

import com.example.nepalweatherwidget.data.remote.model.WeatherResponse
import com.example.nepalweatherwidget.data.remote.model.ForecastResponse
import com.example.nepalweatherwidget.features.weather.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.features.weather.domain.model.WeatherData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BaseWeatherMapper @Inject constructor() {
    
    fun toWeatherData(response: WeatherResponse): WeatherData {
        return WeatherData(
            temperature = response.main.temp,
            feelsLike = response.main.feelsLike,
            description = response.weather.firstOrNull()?.description ?: "",
            iconCode = response.weather.firstOrNull()?.icon ?: "",
            humidity = response.main.humidity,
            windSpeed = response.wind.speed,
            timestamp = response.timestamp * 1000,
            location = response.name,
            pressure = response.main.pressure,
            visibility = response.visibility,
            cloudiness = response.clouds.all,
            sunrise = response.sys.sunrise,
            sunset = response.sys.sunset
        )
    }
    
    fun toWeatherData(entity: WeatherEntity): WeatherData {
        return WeatherData(
            temperature = entity.temperature,
            feelsLike = entity.feelsLike,
            description = entity.description,
            iconCode = entity.iconCode,
            humidity = entity.humidity,
            windSpeed = entity.windSpeed,
            timestamp = entity.timestamp,
            location = entity.location
        )
    }
    
    fun toWeatherEntity(response: WeatherResponse): WeatherEntity {
        return WeatherEntity(
            location = response.name,
            temperature = response.main.temp,
            feelsLike = response.main.feelsLike,
            description = response.weather.firstOrNull()?.description ?: "",
            iconCode = response.weather.firstOrNull()?.icon ?: "",
            humidity = response.main.humidity,
            windSpeed = response.wind.speed,
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun toWeatherDataList(response: ForecastResponse): List<WeatherData> {
        return response.list.map { forecastItem ->
            WeatherData(
                temperature = forecastItem.main.temp,
                feelsLike = forecastItem.main.feelsLike,
                description = forecastItem.weather.firstOrNull()?.description ?: "",
                iconCode = forecastItem.weather.firstOrNull()?.icon ?: "",
                humidity = forecastItem.main.humidity,
                windSpeed = forecastItem.wind.speed,
                timestamp = forecastItem.dt * 1000
            )
        }
    }
} 
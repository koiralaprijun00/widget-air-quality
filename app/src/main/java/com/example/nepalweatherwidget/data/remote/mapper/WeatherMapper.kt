package com.example.nepalweatherwidget.data.remote.mapper

import com.example.nepalweatherwidget.data.remote.model.WeatherResponse
import com.example.nepalweatherwidget.domain.model.WeatherData

fun WeatherResponse.toWeatherData(): WeatherData {
    val weather = weather.firstOrNull()
    return WeatherData(
        temperature = main.temp,
        feelsLike = main.feelsLike,
        description = weather?.description ?: "",
        iconCode = weather?.icon ?: "",
        humidity = main.humidity,
        windSpeed = wind.speed,
        timestamp = timestamp
    )
} 
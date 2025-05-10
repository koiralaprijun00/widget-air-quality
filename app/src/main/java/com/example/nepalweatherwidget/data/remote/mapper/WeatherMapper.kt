package com.example.nepalweatherwidget.data.remote.mapper

import com.example.nepalweatherwidget.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.data.remote.model.WeatherResponse
import com.example.nepalweatherwidget.domain.model.WeatherData

fun WeatherResponse.toWeatherData(): WeatherData {
    return WeatherData(
        temperature = main.temp,
        feelsLike = main.feelsLike,
        description = weather.firstOrNull()?.description ?: "",
        iconCode = weather.firstOrNull()?.icon ?: "",
        humidity = main.humidity,
        windSpeed = wind.speed,
        timestamp = timestamp,
        location = name,
        pressure = main.pressure,
        visibility = visibility,
        cloudiness = clouds.all,
        sunrise = sys.sunrise,
        sunset = sys.sunset
    )
}

fun WeatherData.toEntity(location: String): WeatherEntity {
    return WeatherEntity(
        location = location,
        temperature = temperature,
        feelsLike = feelsLike,
        description = description,
        iconCode = iconCode,
        humidity = humidity,
        windSpeed = windSpeed,
        timestamp = timestamp
    )
}

fun WeatherEntity.toWeatherData(): WeatherData {
    return WeatherData(
        temperature = temperature,
        feelsLike = feelsLike,
        description = description,
        iconCode = iconCode,
        humidity = humidity,
        windSpeed = windSpeed,
        timestamp = timestamp,
        location = location
    )
} 
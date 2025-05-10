package com.example.nepalweatherwidget.domain.model

data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long,
    val location: String = "",
    val pressure: Int = 0,
    val visibility: Int = 0,
    val cloudiness: Int = 0,
    val sunrise: Long = 0,
    val sunset: Long = 0
) 
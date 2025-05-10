package com.example.nepalweatherwidget.domain.model

data class WeatherData(
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long
) 
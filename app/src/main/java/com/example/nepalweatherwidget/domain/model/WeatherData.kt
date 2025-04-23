package com.example.nepalweatherwidget.domain.model

data class WeatherData(
    val temperature: Double,
    val humidity: Int,
    val description: String,
    val location: String,
    val windSpeed: Double,
    val timestamp: Long = System.currentTimeMillis()
) 
package com.example.nepalweatherwidget.data.model

data class WeatherData(
    val location: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double
) 
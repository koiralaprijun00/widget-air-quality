package com.example.nepalweatherwidget.domain.model

data class ForecastItem(
    val hour: String,
    val temperature: Double,
    val aqi: Int,
    val aqiEmoji: String,
    val weatherIconRes: Int
) 
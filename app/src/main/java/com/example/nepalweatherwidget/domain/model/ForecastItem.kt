package com.example.nepalweatherwidget.domain.model

data class ForecastItem(
    val hour: String,
    val aqi: Int,
    val aqiEmoji: String,
    val weatherIconRes: Int,
    val temperature: Int
) 
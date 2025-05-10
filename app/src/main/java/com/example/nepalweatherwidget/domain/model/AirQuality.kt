package com.example.nepalweatherwidget.domain.model

data class AirQuality(
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val timestamp: Long
) 
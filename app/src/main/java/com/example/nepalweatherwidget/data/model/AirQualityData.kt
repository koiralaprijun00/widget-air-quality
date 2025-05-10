package com.example.nepalweatherwidget.data.model

data class AirQualityData(
    val aqi: Int,
    val location: String,
    val timestamp: Long = System.currentTimeMillis(),
    val pollutants: Pollutants = Pollutants()
)

data class Pollutants(
    val pm2_5: Double = 0.0,
    val pm10: Double = 0.0,
    val o3: Double = 0.0,
    val no2: Double = 0.0,
    val so2: Double = 0.0,
    val co: Double = 0.0
) 
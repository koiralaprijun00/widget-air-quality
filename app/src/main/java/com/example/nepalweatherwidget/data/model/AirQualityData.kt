package com.example.nepalweatherwidget.data.model

data class AirQualityData(
    val aqi: Int,
    val statusRes: Int,
    val adviceRes: Int,
    val healthRes: Int,
    val nearbyLocations: List<String> = emptyList()
) 
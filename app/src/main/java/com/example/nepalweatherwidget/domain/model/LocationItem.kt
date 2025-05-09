package com.example.nepalweatherwidget.domain.model

data class LocationItem(
    val locationName: String,
    val locationSub: String,
    val aqi: Int,
    val weatherIconRes: Int,
    val temperature: Int
) 
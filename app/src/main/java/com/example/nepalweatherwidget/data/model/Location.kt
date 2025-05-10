package com.example.nepalweatherwidget.data.model

data class Location(
    val id: String,
    val name: String,
    val temperature: Double,
    val weatherDescription: String,
    val latitude: Double,
    val longitude: Double
) 
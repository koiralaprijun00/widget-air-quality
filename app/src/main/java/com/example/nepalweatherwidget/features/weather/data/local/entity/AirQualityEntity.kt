package com.example.nepalweatherwidget.features.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "air_quality")
data class AirQualityEntity(
    @PrimaryKey
    val location: String,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val timestamp: Long = System.currentTimeMillis()
) 
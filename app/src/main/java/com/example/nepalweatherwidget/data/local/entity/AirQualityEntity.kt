package com.example.nepalweatherwidget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "air_quality_data")
data class AirQualityEntity(
    @PrimaryKey
    val location: String,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val timestamp: Long
) 
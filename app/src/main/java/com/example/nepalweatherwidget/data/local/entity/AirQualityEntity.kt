package com.example.nepalweatherwidget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nepalweatherwidget.data.remote.model.AirQualityResponse

@Entity(tableName = "air_quality_data")
data class AirQualityEntity(
    @PrimaryKey
    val location: String,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val timestamp: Long
) {
    companion object {
        fun fromResponse(response: AirQualityResponse): AirQualityEntity {
            val firstData = response.list.firstOrNull()
            return AirQualityEntity(
                location = "", // Need to set location
                aqi = firstData?.main?.aqi ?: 0,
                pm25 = firstData?.components?.pm25 ?: 0.0,
                pm10 = firstData?.components?.pm10 ?: 0.0,
                timestamp = System.currentTimeMillis()
            )
        }
    }
} 
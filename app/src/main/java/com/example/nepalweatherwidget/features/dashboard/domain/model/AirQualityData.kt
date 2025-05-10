package com.example.nepalweatherwidget.domain.model

data class AirQualityData(
    val location: String,
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val timestamp: Long
) {
    companion object {
        fun fromEntity(entity: com.example.nepalweatherwidget.data.local.entity.AirQualityEntity): AirQualityData {
            return AirQualityData(
                location = entity.location,
                aqi = entity.aqi,
                pm25 = entity.pm25,
                pm10 = entity.pm10,
                timestamp = entity.timestamp
            )
        }
    }
} 
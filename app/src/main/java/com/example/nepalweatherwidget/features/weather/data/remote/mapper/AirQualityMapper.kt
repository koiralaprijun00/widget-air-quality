package com.example.nepalweatherwidget.data.remote.mapper

import com.example.nepalweatherwidget.data.local.entity.AirQualityEntity
import com.example.nepalweatherwidget.data.remote.model.AirQualityResponse
import com.example.nepalweatherwidget.domain.model.AirQuality

fun AirQualityResponse.AirQualityData.toAirQuality(): AirQuality {
    return AirQuality(
        aqi = main.aqi,
        pm25 = components.pm25,
        pm10 = components.pm10,
        timestamp = timestamp
    )
}

fun AirQuality.toEntity(location: String): AirQualityEntity {
    return AirQualityEntity(
        location = location,
        aqi = aqi,
        pm25 = pm25,
        pm10 = pm10,
        timestamp = timestamp
    )
}

fun AirQualityEntity.toAirQuality(): AirQuality {
    return AirQuality(
        aqi = aqi,
        pm25 = pm25,
        pm10 = pm10,
        timestamp = timestamp
    )
} 
package com.example.nepalweatherwidget.features.airquality.data.mapper

import com.example.nepalweatherwidget.data.remote.model.AirQualityResponse
import com.example.nepalweatherwidget.features.airquality.data.local.entity.AirQualityEntity
import com.example.nepalweatherwidget.features.airquality.domain.model.AirQuality
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AirQualityMapper @Inject constructor() {
    
    fun toDomain(response: AirQualityResponse): AirQuality {
        val components = response.list.firstOrNull()?.components
        val main = response.list.firstOrNull()?.main
        
        return AirQuality(
            aqi = main?.aqi ?: 0,
            pm25 = components?.pm25 ?: 0.0,
            pm10 = components?.pm10 ?: 0.0,
            timestamp = System.currentTimeMillis()
        )
    }
    
    fun toDomain(entity: AirQualityEntity): AirQuality {
        return AirQuality(
            aqi = entity.aqi,
            pm25 = entity.pm25,
            pm10 = entity.pm10,
            timestamp = entity.timestamp
        )
    }
    
    fun toEntity(response: AirQualityResponse): AirQualityEntity {
        val components = response.list.firstOrNull()?.components
        val main = response.list.firstOrNull()?.main
        
        return AirQualityEntity(
            location = "", // TODO: Get location name
            aqi = main?.aqi ?: 0,
            pm25 = components?.pm25 ?: 0.0,
            pm10 = components?.pm10 ?: 0.0,
            timestamp = System.currentTimeMillis()
        )
    }
} 
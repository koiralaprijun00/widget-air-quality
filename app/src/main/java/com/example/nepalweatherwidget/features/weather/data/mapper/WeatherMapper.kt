package com.example.nepalweatherwidget.features.weather.data.mapper

import com.example.nepalweatherwidget.data.remote.model.WeatherResponse
import com.example.nepalweatherwidget.features.weather.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.features.weather.domain.model.WeatherData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeatherMapper @Inject constructor(
    private val baseMapper: BaseWeatherMapper
) {
    fun toDomain(response: WeatherResponse): WeatherData {
        return baseMapper.toWeatherData(response)
    }
    
    fun toDomain(entity: WeatherEntity): WeatherData {
        return baseMapper.toWeatherData(entity)
    }
    
    fun toEntity(response: WeatherResponse): WeatherEntity {
        return baseMapper.toWeatherEntity(response)
    }
} 
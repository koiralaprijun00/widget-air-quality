package com.example.nepalweatherwidget.features.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nepalweatherwidget.data.remote.model.WeatherResponse

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val location: String,
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromResponse(response: WeatherResponse): WeatherEntity {
            return WeatherEntity(
                location = response.name,
                temperature = response.main.temp,
                feelsLike = response.main.feelsLike,
                description = response.weather.firstOrNull()?.description ?: "",
                iconCode = response.weather.firstOrNull()?.icon ?: "",
                humidity = response.main.humidity,
                windSpeed = response.wind.speed
            )
        }
    }
} 
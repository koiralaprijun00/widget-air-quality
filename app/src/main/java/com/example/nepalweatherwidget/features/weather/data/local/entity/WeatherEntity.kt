package com.example.nepalweatherwidget.features.weather.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nepalweatherwidget.data.remote.model.WeatherResponse

@Entity(tableName = "weather")
data class WeatherEntity(
    @PrimaryKey
    val location: String,
    val temperature: Double,
    val description: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun fromResponse(response: WeatherResponse): WeatherEntity {
            return WeatherEntity(
                location = response.name,
                temperature = response.main.temp,
                description = response.weather.firstOrNull()?.description ?: "",
                humidity = response.main.humidity,
                windSpeed = response.wind.speed
            )
        }
    }
} 
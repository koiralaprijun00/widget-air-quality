package com.example.nepalweatherwidget.presentation.model

import android.graphics.Color
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.R

data class AirQualityUiState(
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val status: String,
    val statusColor: Int
) {
    companion object {
        fun fromAirQuality(airQuality: AirQuality): AirQualityUiState {
            val (status, color) = when (airQuality.aqi) {
                in 0..50 -> Pair("Good", Color.GREEN)
                in 51..100 -> Pair("Moderate", Color.YELLOW)
                in 101..150 -> Pair("Unhealthy for Sensitive Groups", Color.rgb(255, 165, 0)) // Orange
                in 151..200 -> Pair("Unhealthy", Color.RED)
                in 201..300 -> Pair("Very Unhealthy", Color.rgb(128, 0, 128)) // Purple
                else -> Pair("Hazardous", Color.rgb(128, 0, 0)) // Maroon
            }
            
            return AirQualityUiState(
                aqi = airQuality.aqi,
                pm25 = airQuality.pm25,
                pm10 = airQuality.pm10,
                status = status,
                statusColor = color
            )
        }
    }
} 
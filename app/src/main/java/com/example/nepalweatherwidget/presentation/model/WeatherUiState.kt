package com.example.nepalweatherwidget.presentation.model

import com.example.nepalweatherwidget.domain.model.WeatherData

data class WeatherUiState(
    val temperature: Double,
    val feelsLike: Double,
    val description: String,
    val iconCode: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long
) {
    companion object {
        fun fromDomain(weatherData: WeatherData): WeatherUiState {
            return WeatherUiState(
                temperature = weatherData.temperature,
                feelsLike = weatherData.feelsLike,
                description = weatherData.description,
                iconCode = weatherData.iconCode,
                humidity = weatherData.humidity,
                windSpeed = weatherData.windSpeed,
                timestamp = weatherData.timestamp
            )
        }
    }
} 
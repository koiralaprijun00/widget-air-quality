package com.example.nepalweatherwidget.domain.usecase

import com.example.nepalweatherwidget.domain.model.WeatherData

object GetWeatherUseCase {
    fun getMockWeatherData(): WeatherData {
        return WeatherData(
            temperature = 25.0,
            humidity = 65,
            description = "Partly Cloudy",
            location = "Kathmandu",
            windSpeed = 3.5,
            timestamp = System.currentTimeMillis()
        )
    }
} 
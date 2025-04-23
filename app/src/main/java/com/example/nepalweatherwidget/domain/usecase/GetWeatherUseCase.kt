package com.example.nepalweatherwidget.domain.usecase

import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherUseCase @Inject constructor(
    private val repository: WeatherRepository
) {
    suspend operator fun invoke(location: String): Result<WeatherData> {
        return repository.getWeatherData(location)
    }

    suspend fun getCachedWeather(): Result<WeatherData> {
        return repository.getCachedWeatherData()
    }
} 
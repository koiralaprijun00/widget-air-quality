package com.example.nepalweatherwidget.features.weather.domain.usecase

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.weather.domain.model.WeatherData
import com.example.nepalweatherwidget.features.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetCurrentWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val errorHandler: ErrorHandler
) {
    suspend operator fun invoke(location: String): Result<WeatherData> {
        return try {
            weatherRepository.getWeatherData(location)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
} 
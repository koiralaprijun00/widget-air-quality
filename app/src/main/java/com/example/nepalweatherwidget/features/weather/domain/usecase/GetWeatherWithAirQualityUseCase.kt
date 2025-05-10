package com.example.nepalweatherwidget.features.weather.domain.usecase

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.weather.domain.model.AirQuality
import com.example.nepalweatherwidget.features.weather.domain.model.WeatherData
import com.example.nepalweatherwidget.features.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetWeatherWithAirQualityUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val errorHandler: ErrorHandler
) {
    suspend operator fun invoke(location: String): Result<Pair<WeatherData, AirQuality>> {
        return try {
            weatherRepository.getWeatherAndAirQuality(location)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
} 
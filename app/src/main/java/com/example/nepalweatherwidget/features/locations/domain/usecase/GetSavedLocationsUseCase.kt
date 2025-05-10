package com.example.nepalweatherwidget.features.locations.domain.usecase

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.weather.domain.model.Location
import com.example.nepalweatherwidget.features.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetSavedLocationsUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val errorHandler: ErrorHandler
) {
    suspend operator fun invoke(): Result<List<Location>> {
        return try {
            weatherRepository.getSavedLocations()
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
} 
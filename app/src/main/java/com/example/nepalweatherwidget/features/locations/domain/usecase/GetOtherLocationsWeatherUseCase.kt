package com.example.nepalweatherwidget.features.locations.domain.usecase

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.locations.domain.model.LocationItem
import com.example.nepalweatherwidget.features.weather.domain.repository.WeatherRepository
import javax.inject.Inject

class GetOtherLocationsWeatherUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val errorHandler: ErrorHandler
) {
    suspend operator fun invoke(locations: List<String>): Result<List<LocationItem>> {
        return try {
            val locationItems = mutableListOf<LocationItem>()
            
            for (location in locations) {
                when (val result = weatherRepository.getWeatherData(location)) {
                    is Result.Success -> {
                        locationItems.add(
                            LocationItem(
                                locationName = location,
                                temperature = result.data.temperature,
                                locationSub = result.data.description
                            )
                        )
                    }
                    is Result.Error -> {
                        // Skip failed locations
                        continue
                    }
                }
            }
            
            Result.Success(locationItems)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
} 
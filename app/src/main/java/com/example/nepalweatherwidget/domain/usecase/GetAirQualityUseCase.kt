package com.example.nepalweatherwidget.domain.usecase

import com.example.widget_air_quality.data.repository.AirPollutionRepository
import javax.inject.Inject

class GetAirQualityUseCase @Inject constructor(
    private val repository: AirPollutionRepository
) {
    suspend fun getCurrentAirQuality(lat: Double, lon: Double) = 
        repository.getCurrentAirPollution(lat, lon)

    suspend fun getAirQualityForecast(lat: Double, lon: Double) =
        repository.getAirPollutionForecast(lat, lon)
} 
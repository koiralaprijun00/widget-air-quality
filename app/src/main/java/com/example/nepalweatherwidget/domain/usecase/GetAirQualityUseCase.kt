package com.example.nepalweatherwidget.domain.usecase

import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import javax.inject.Inject

class GetAirQualityUseCase @Inject constructor(
    private val weatherRepository: WeatherRepository
) {
    suspend operator fun invoke(lat: Double, lon: Double) = weatherRepository.getAirQuality(lat, lon)
} 
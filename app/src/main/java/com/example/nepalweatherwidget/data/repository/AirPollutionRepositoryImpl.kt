package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.model.AirPollutionData
import com.example.nepalweatherwidget.domain.repository.AirPollutionRepository
import javax.inject.Inject

class AirPollutionRepositoryImpl @Inject constructor(
    private val airPollutionService: AirPollutionService
) : AirPollutionRepository {

    override suspend fun getCurrentAirQuality(lat: Double, lon: Double): AirPollutionData {
        return try {
            val response = airPollutionService.getCurrentAirQuality(lat, lon)
            val airQuality = response.list.firstOrNull()
                ?: throw Exception("No air quality data available")

            AirPollutionData(
                aqi = airQuality.main.aqi,
                pm25 = airQuality.components.pm2_5,
                pm10 = airQuality.components.pm10,
                description = getAirQualityDescription(airQuality.main.aqi)
            )
        } catch (e: Exception) {
            throw Exception("Failed to fetch air quality data: ${e.message}")
        }
    }

    private fun getAirQualityDescription(aqi: Int): String {
        return when (aqi) {
            1 -> "Good"
            2 -> "Fair"
            3 -> "Moderate"
            4 -> "Poor"
            5 -> "Very Poor"
            else -> "Unknown"
        }
    }
} 
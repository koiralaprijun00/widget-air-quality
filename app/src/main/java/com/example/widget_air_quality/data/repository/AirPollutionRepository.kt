package com.example.widget_air_quality.data.repository

import com.example.widget_air_quality.data.api.AirPollutionService
import com.example.widget_air_quality.data.model.AirPollutionResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AirPollutionRepository(
    private val service: AirPollutionService,
    private val apiKey: String
) {
    suspend fun getCurrentAirPollution(lat: Double, lon: Double): Result<AirPollutionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = service.getCurrentAirPollution(lat, lon, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAirPollutionForecast(lat: Double, lon: Double): Result<AirPollutionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = service.getAirPollutionForecast(lat, lon, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAirPollutionHistory(lat: Double, lon: Double, start: Long, end: Long): Result<AirPollutionResponse> = withContext(Dispatchers.IO) {
        try {
            val response = service.getAirPollutionHistory(lat, lon, start, end, apiKey)
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 
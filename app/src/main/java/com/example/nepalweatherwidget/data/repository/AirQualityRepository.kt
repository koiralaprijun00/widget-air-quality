package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.data.model.AirQualityData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AirQualityRepository @Inject constructor() {
    
    fun getAirQualityData(latitude: Double, longitude: Double): Flow<AirQualityData> = flow {
        // TODO: Implement actual API call to fetch air quality data
        // For now, return mock data
        emit(
            AirQualityData(
                aqi = 75,
                location = "Kathmandu",
                pollutants = Pollutants(
                    pm2_5 = 25.5,
                    pm10 = 45.2,
                    o3 = 32.1,
                    no2 = 15.8,
                    so2 = 8.4,
                    co = 0.8
                )
            )
        )
    }
} 
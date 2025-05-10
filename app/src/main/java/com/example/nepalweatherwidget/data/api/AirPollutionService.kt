package com.example.nepalweatherwidget.data.api

import com.example.nepalweatherwidget.data.model.AirPollutionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AirPollutionService {
    @GET("data/2.5/air_pollution")
    suspend fun getCurrentAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String = BuildConfig.OPENWEATHER_API_KEY
    ): AirPollutionResponse
} 
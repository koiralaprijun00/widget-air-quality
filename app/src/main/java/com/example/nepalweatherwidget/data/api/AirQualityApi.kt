package com.example.nepalweatherwidget.data.api

import com.example.nepalweatherwidget.data.model.AirQualityResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AirQualityApi {
    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): AirQualityResponse
} 
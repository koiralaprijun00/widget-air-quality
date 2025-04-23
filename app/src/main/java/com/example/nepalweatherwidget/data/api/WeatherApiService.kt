package com.example.nepalweatherwidget.data.api

import com.example.nepalweatherwidget.data.model.AirPollutionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {
    @GET("data/2.5/air_pollution")
    suspend fun getAirPollution(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String
    ): AirPollutionResponse
} 
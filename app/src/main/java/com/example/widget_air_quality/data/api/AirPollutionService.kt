package com.example.widget_air_quality.data.api

import com.example.widget_air_quality.data.model.AirPollutionResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface AirPollutionService {
    @GET("data/2.5/air_pollution")
    suspend fun getCurrentAirPollution(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirPollutionResponse

    @GET("data/2.5/air_pollution/forecast")
    suspend fun getAirPollutionForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AirPollutionResponse

    @GET("data/2.5/air_pollution/history")
    suspend fun getAirPollutionHistory(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("start") start: Long,
        @Query("end") end: Long,
        @Query("appid") apiKey: String
    ): AirPollutionResponse
} 
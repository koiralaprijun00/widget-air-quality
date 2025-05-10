package com.example.nepalweatherwidget.data.remote.api

import com.example.nepalweatherwidget.data.remote.model.GeocodingResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GeocodingService {
    @GET("geo/1.0/direct")
    suspend fun getCoordinatesByLocationName(
        @Query("q") locationName: String,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<GeocodingResponse>
    
    @GET("geo/1.0/reverse")
    suspend fun getLocationNameByCoordinates(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("limit") limit: Int = 5,
        @Query("appid") apiKey: String
    ): List<GeocodingResponse>
    
    @GET("geo/1.0/zip")
    suspend fun getCoordinatesByZipCode(
        @Query("zip") zipCode: String,
        @Query("appid") apiKey: String
    ): GeocodingResponse
} 
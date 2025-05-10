package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.Location

interface GeocodingRepository {
    suspend fun getLocationByName(locationName: String): Result<Location>
    suspend fun getLocationsByName(locationName: String, limit: Int = 5): Result<List<Location>>
    suspend fun getLocationByCoordinates(latitude: Double, longitude: Double): Result<Location>
    suspend fun getLocationByZipCode(zipCode: String): Result<Location>
} 
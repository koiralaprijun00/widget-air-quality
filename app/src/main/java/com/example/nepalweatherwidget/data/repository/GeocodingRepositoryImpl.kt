package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.extension.withNetworkRetry
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.data.remote.api.GeocodingService
import com.example.nepalweatherwidget.domain.model.Location
import com.example.nepalweatherwidget.domain.repository.GeocodingRepository
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class GeocodingRepositoryImpl @Inject constructor(
    private val geocodingService: GeocodingService,
    private val errorHandler: ErrorHandler,
    @Named("openweather_api_key") private val apiKey: String
) : GeocodingRepository {
    
    override suspend fun getLocationByName(locationName: String): Result<Location> {
        return try {
            val response = withNetworkRetry {
                geocodingService.getCoordinatesByLocationName(
                    locationName = locationName,
                    limit = 1,
                    apiKey = apiKey
                )
            }
            
            if (response.isNotEmpty()) {
                Result.Success(Location.fromGeocodingResponse(response.first()))
            } else {
                Result.Error(WeatherException.LocationException.LocationNotFound("Location not found: $locationName"))
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun getLocationsByName(locationName: String, limit: Int): Result<List<Location>> {
        return try {
            val response = withNetworkRetry {
                geocodingService.getCoordinatesByLocationName(
                    locationName = locationName,
                    limit = limit,
                    apiKey = apiKey
                )
            }
            
            val locations = response.map { Location.fromGeocodingResponse(it) }
            Result.Success(locations)
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun getLocationByCoordinates(latitude: Double, longitude: Double): Result<Location> {
        return try {
            val response = withNetworkRetry {
                geocodingService.getLocationNameByCoordinates(
                    latitude = latitude,
                    longitude = longitude,
                    limit = 1,
                    apiKey = apiKey
                )
            }
            
            if (response.isNotEmpty()) {
                Result.Success(Location.fromGeocodingResponse(response.first()))
            } else {
                Result.Error(WeatherException.LocationException.LocationNotFound("Location not found for coordinates: $latitude, $longitude"))
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
    
    override suspend fun getLocationByZipCode(zipCode: String): Result<Location> {
        return try {
            val response = withNetworkRetry {
                geocodingService.getCoordinatesByZipCode(
                    zipCode = zipCode,
                    apiKey = apiKey
                )
            }
            
            Result.Success(Location.fromGeocodingResponse(response))
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
} 
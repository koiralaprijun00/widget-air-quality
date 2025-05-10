package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.Location

/**
 * Repository interface for weather and air quality data.
 * All methods are suspend functions returning Result types for consistent error handling.
 */
interface WeatherRepository {
    /**
     * Get weather data for a location by name
     * @param locationName The name of the location
     * @return Result containing WeatherData or error
     */
    suspend fun getWeatherData(locationName: String): Result<WeatherData>
    
    /**
     * Get air quality data for a location by name
     * @param locationName The name of the location
     * @return Result containing AirQuality or error
     */
    suspend fun getAirQuality(locationName: String): Result<AirQuality>
    
    /**
     * Get both weather and air quality data for a location by name
     * @param locationName The name of the location
     * @return Result containing Pair of WeatherData and AirQuality or error
     */
    suspend fun getWeatherAndAirQuality(locationName: String): Result<Pair<WeatherData, AirQuality>>
    
    /**
     * Get weather data for a location by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @return Result containing WeatherData or error
     */
    suspend fun getWeatherDataByCoordinates(lat: Double, lon: Double): Result<WeatherData>
    
    /**
     * Get air quality data for a location by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @return Result containing AirQuality or error
     */
    suspend fun getAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQuality>
    
    /**
     * Get both weather and air quality data for a location by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @return Result containing Pair of WeatherData and AirQuality or error
     */
    suspend fun getWeatherAndAirQualityByCoordinates(lat: Double, lon: Double): Result<Pair<WeatherData, AirQuality>>
    
    /**
     * Get weather forecast for a location
     * @param locationName The name of the location
     * @param days Number of days to forecast (default: 5)
     * @return Result containing list of WeatherData or error
     */
    suspend fun getForecast(locationName: String, days: Int = 5): Result<List<WeatherData>>
    
    /**
     * Get weather forecast for a location by coordinates
     * @param lat Latitude
     * @param lon Longitude
     * @param days Number of days to forecast (default: 5)
     * @return Result containing list of WeatherData or error
     */
    suspend fun getForecastByCoordinates(lat: Double, lon: Double, days: Int = 5): Result<List<WeatherData>>
    
    /**
     * Get list of saved locations
     * @return Result containing list of Location or error
     */
    suspend fun getSavedLocations(): Result<List<Location>>
    
    /**
     * Save a location
     * @param location Location to save
     * @return Result containing saved Location or error
     */
    suspend fun saveLocation(location: Location): Result<Location>
    
    /**
     * Delete a saved location
     * @param locationId ID of location to delete
     * @return Result containing success or error
     */
    suspend fun deleteLocation(locationId: String): Result<Unit>
    
    /**
     * Clear all cached data
     * @return Result containing success or error
     */
    suspend fun clearCache(): Result<Unit>
} 
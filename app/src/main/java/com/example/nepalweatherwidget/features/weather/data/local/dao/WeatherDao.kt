package com.example.nepalweatherwidget.features.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nepalweatherwidget.features.weather.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.features.weather.data.local.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather WHERE location = :location")
    fun getWeatherByLocation(location: String): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    @Query("DELETE FROM weather WHERE location = :location")
    suspend fun deleteWeatherByLocation(location: String)

    @Query("SELECT * FROM weather_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatestWeatherData(): Flow<WeatherEntity?>

    @Query("SELECT * FROM weather_data WHERE timestamp > :timestamp")
    fun getRecentWeatherData(timestamp: Long): Flow<List<WeatherEntity>>

    // Location-related methods
    @Query("SELECT * FROM saved_locations ORDER BY timestamp DESC")
    fun getSavedLocations(): Flow<List<LocationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: LocationEntity)

    @Query("DELETE FROM saved_locations WHERE id = :locationId")
    suspend fun deleteLocation(locationId: String)
} 
package com.example.nepalweatherwidget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nepalweatherwidget.data.local.entity.WeatherEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_data WHERE location = :location")
    fun getWeatherData(location: String): Flow<WeatherEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeatherData(weather: WeatherEntity)

    @Query("DELETE FROM weather_data WHERE location = :location")
    suspend fun deleteWeatherData(location: String)

    @Query("SELECT * FROM weather_data WHERE timestamp < :timestamp")
    suspend fun getOldWeatherData(timestamp: Long): List<WeatherEntity>
} 
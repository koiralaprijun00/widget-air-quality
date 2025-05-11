package com.example.nepalweatherwidget.features.weather.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nepalweatherwidget.features.weather.data.local.entity.AirQualityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AirQualityDao {
    @Query("SELECT * FROM air_quality WHERE location = :location")
    fun getAirQualityData(location: String): Flow<AirQualityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirQuality(airQuality: AirQualityEntity)

    @Query("DELETE FROM air_quality WHERE location = :location")
    suspend fun deleteAirQualityByLocation(location: String)

    @Query("SELECT * FROM air_quality ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAirQualityData(): Flow<AirQualityEntity?>

    @Query("SELECT * FROM air_quality WHERE timestamp > :timestamp")
    fun getRecentAirQualityData(timestamp: Long): Flow<List<AirQualityEntity>>

    @Query("DELETE FROM air_quality")
    suspend fun clearAll()
} 
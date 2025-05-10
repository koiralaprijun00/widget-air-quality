package com.example.nepalweatherwidget.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.nepalweatherwidget.data.local.entity.AirQualityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AirQualityDao {
    @Query("SELECT * FROM air_quality_data WHERE location = :location")
    fun getAirQualityData(location: String): Flow<AirQualityEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAirQualityData(airQuality: AirQualityEntity)

    @Query("DELETE FROM air_quality_data WHERE location = :location")
    suspend fun deleteAirQualityData(location: String)

    @Query("SELECT * FROM air_quality_data ORDER BY timestamp DESC LIMIT 1")
    fun getLatestAirQualityData(): Flow<AirQualityEntity?>

    @Query("SELECT * FROM air_quality_data WHERE timestamp > :timestamp")
    fun getRecentAirQualityData(timestamp: Long): Flow<List<AirQualityEntity>>
} 
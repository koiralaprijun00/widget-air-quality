package com.example.nepalweatherwidget.features.airquality.data.repository

import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.airquality.data.datasource.local.AirQualityLocalDataSource
import com.example.nepalweatherwidget.features.airquality.data.datasource.remote.AirQualityRemoteDataSource
import com.example.nepalweatherwidget.features.airquality.data.mapper.AirQualityMapper
import com.example.nepalweatherwidget.features.airquality.domain.model.AirQuality
import com.example.nepalweatherwidget.features.airquality.domain.repository.AirQualityRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AirQualityRepositoryImpl @Inject constructor(
    private val remoteDataSource: AirQualityRemoteDataSource,
    private val localDataSource: AirQualityLocalDataSource,
    private val airQualityMapper: AirQualityMapper
) : AirQualityRepository {
    
    override suspend fun getAirQuality(locationName: String): Result<AirQuality> {
        return try {
            // Try remote first
            val remoteResult = remoteDataSource.getAirQuality(locationName)
            
            if (remoteResult is Result.Success) {
                // Cache the data
                localDataSource.saveAirQuality(remoteResult.data)
                Result.Success(airQualityMapper.toDomain(remoteResult.data))
            } else {
                // Fallback to cache
                val cachedData = localDataSource.getAirQuality(locationName)
                if (cachedData != null) {
                    Result.Success(airQualityMapper.toDomain(cachedData))
                } else {
                    remoteResult
                }
            }
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }

    override suspend fun getAirQualityByCoordinates(lat: Double, lon: Double): Result<AirQuality> {
        return try {
            val remoteResult = remoteDataSource.getAirQualityByCoordinates(lat, lon)
            
            if (remoteResult is Result.Success) {
                localDataSource.saveAirQuality(remoteResult.data)
                Result.Success(airQualityMapper.toDomain(remoteResult.data))
            } else {
                remoteResult
            }
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }

    override suspend fun clearCache(): Result<Unit> {
        return try {
            localDataSource.clearCache()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }
} 
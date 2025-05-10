package com.example.nepalweatherwidget.features.locations.data.repository

import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.locations.data.datasource.local.LocationLocalDataSource
import com.example.nepalweatherwidget.features.locations.data.mapper.LocationMapper
import com.example.nepalweatherwidget.features.locations.domain.model.Location
import com.example.nepalweatherwidget.features.locations.domain.repository.LocationRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepositoryImpl @Inject constructor(
    private val localDataSource: LocationLocalDataSource,
    private val locationMapper: LocationMapper
) : LocationRepository {
    
    override suspend fun getSavedLocations(): Result<List<Location>> {
        return try {
            val locations = localDataSource.getSavedLocations()
            Result.Success(locations.map { locationMapper.toDomain(it) })
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }

    override suspend fun saveLocation(location: Location): Result<Location> {
        return try {
            val entity = locationMapper.toEntity(location)
            localDataSource.saveLocation(entity)
            Result.Success(location)
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }

    override suspend fun deleteLocation(locationId: String): Result<Unit> {
        return try {
            localDataSource.deleteLocation(locationId)
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }

    override suspend fun clearLocations(): Result<Unit> {
        return try {
            localDataSource.clearLocations()
            Result.Success(Unit)
        } catch (e: Exception) {
            Result.Error(WeatherException.UnknownError(e.message ?: ""))
        }
    }
} 
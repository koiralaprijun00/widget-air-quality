package com.example.nepalweatherwidget.features.locations.data.mapper

import com.example.nepalweatherwidget.data.remote.model.GeocodingResponse
import com.example.nepalweatherwidget.features.locations.data.local.entity.LocationEntity
import com.example.nepalweatherwidget.features.locations.domain.model.Location
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationMapper @Inject constructor() {
    
    fun toDomain(response: GeocodingResponse): Location {
        return Location(
            id = "${response.lat}_${response.lon}",
            name = response.name,
            latitude = response.lat,
            longitude = response.lon,
            country = response.country,
            state = response.state
        )
    }
    
    fun toDomain(entity: LocationEntity): Location {
        return Location(
            id = entity.id,
            name = entity.name,
            latitude = entity.latitude,
            longitude = entity.longitude,
            country = entity.country,
            state = entity.state
        )
    }
    
    fun toEntity(location: Location): LocationEntity {
        return LocationEntity(
            id = location.id,
            name = location.name,
            latitude = location.latitude,
            longitude = location.longitude,
            country = location.country,
            state = location.state,
            timestamp = System.currentTimeMillis()
        )
    }
} 
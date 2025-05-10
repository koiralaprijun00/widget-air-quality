package com.example.nepalweatherwidget.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.nepalweatherwidget.domain.model.Location

@Entity(tableName = "saved_locations")
data class LocationEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long
) {
    fun toLocation(): Location {
        return Location(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude
        )
    }

    companion object {
        fun fromLocation(location: Location): LocationEntity {
            return LocationEntity(
                id = location.id,
                name = location.name,
                latitude = location.latitude,
                longitude = location.longitude,
                timestamp = System.currentTimeMillis()
            )
        }
    }
} 
package com.example.nepalweatherwidget.domain.model

import com.example.nepalweatherwidget.data.remote.model.GeocodingResponse

data class Location(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val country: String,
    val state: String? = null,
    val displayName: String
) {
    companion object {
        fun fromGeocodingResponse(response: GeocodingResponse): Location {
            val displayName = buildString {
                append(response.name)
                response.state?.let { append(", $it") }
                append(", ${response.country}")
            }
            
            return Location(
                name = response.name,
                latitude = response.latitude,
                longitude = response.longitude,
                country = response.country,
                state = response.state,
                displayName = displayName
            )
        }
    }
} 
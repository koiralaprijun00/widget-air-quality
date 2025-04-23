package com.example.nepalweatherwidget.data.model

data class AirQualityResponse(
    val coord: Coordinates,
    val list: List<AirQualityData>
)

data class Coordinates(
    val lat: Double,
    val lon: Double
)

data class AirQualityData(
    val dt: Long,
    val main: AirQualityIndex,
    val components: AirComponents
)

data class AirQualityIndex(
    val aqi: Int
)

data class AirComponents(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
) 
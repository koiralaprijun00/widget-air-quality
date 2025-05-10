package com.example.widget_air_quality.data.model

data class AirPollutionResponse(
    val coord: Coordinates,
    val list: List<AirPollutionData>
)

data class Coordinates(
    val lat: Double,
    val lon: Double
)

data class AirPollutionData(
    val dt: Long,
    val main: MainData,
    val components: Components
)

data class MainData(
    val aqi: Int
)

data class Components(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
)

enum class AirQualityIndex(val value: Int, val description: String) {
    GOOD(1, "Good"),
    FAIR(2, "Fair"),
    MODERATE(3, "Moderate"),
    POOR(4, "Poor"),
    VERY_POOR(5, "Very Poor");

    companion object {
        fun fromValue(value: Int): AirQualityIndex {
            return values().find { it.value == value } ?: GOOD
        }
    }
} 
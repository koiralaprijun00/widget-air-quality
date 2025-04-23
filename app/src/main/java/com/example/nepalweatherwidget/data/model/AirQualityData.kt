package com.example.nepalweatherwidget.data.model

data class AirQualityData(
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val description: String
)

data class AirQualityResponse(
    val list: List<AirQualityItem>
) {
    data class AirQualityItem(
        val main: Main,
        val components: Components
    )
    
    data class Main(
        val aqi: Int
    )
    
    data class Components(
        val pm2_5: Double,
        val pm10: Double
    )
}

fun AirQualityResponse.toAirQualityData(): AirQualityData {
    val item = list.firstOrNull() ?: throw IllegalStateException("No air quality data available")
    return AirQualityData(
        aqi = item.main.aqi,
        pm25 = item.components.pm2_5,
        pm10 = item.components.pm10,
        description = when (item.main.aqi) {
            1 -> "Good"
            2 -> "Fair"
            3 -> "Moderate"
            4 -> "Poor"
            5 -> "Very Poor"
            else -> "Unknown"
        }
    )
} 
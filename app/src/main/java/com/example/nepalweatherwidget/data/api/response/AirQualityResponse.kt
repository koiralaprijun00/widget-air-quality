package com.example.nepalweatherwidget.data.api.response

data class AirQualityResponse(
    val list: List<AirQualityData>
)

data class AirQualityData(
    val main: Main,
    val components: Components,
    val dt: Long
)

data class Main(
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
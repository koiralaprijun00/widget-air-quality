package com.example.nepalweatherwidget.data.model

data class AirPollutionResponse(
    val list: List<AirQualityData>
)

data class AirQualityData(
    val main: AirQualityMain,
    val components: AirQualityComponents,
    val dt: Long
)

data class AirQualityMain(
    val aqi: Int
)

data class AirQualityComponents(
    val co: Double,
    val no: Double,
    val no2: Double,
    val o3: Double,
    val so2: Double,
    val pm2_5: Double,
    val pm10: Double,
    val nh3: Double
) 
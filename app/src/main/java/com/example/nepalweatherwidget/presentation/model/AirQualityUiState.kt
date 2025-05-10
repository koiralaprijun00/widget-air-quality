package com.example.nepalweatherwidget.presentation.model

data class AirQualityUiState(
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    val status: String,
    val advice: String,
    val healthMessage: String
) {
    companion object {
        fun fromAirQuality(airQuality: com.example.nepalweatherwidget.domain.model.AirQuality): AirQualityUiState {
            val (status, advice, healthMessage) = when (airQuality.aqi) {
                1 -> Triple(
                    "Good",
                    "Enjoy outdoor activities",
                    "Air quality is satisfactory and air pollution poses little or no risk."
                )
                2 -> Triple(
                    "Fair",
                    "Consider limiting outdoor activities",
                    "Air quality is acceptable. However, there may be a risk for some people."
                )
                3 -> Triple(
                    "Moderate",
                    "Limit outdoor activities",
                    "Members of sensitive groups may experience health effects."
                )
                4 -> Triple(
                    "Poor",
                    "Avoid outdoor activities",
                    "Everyone may begin to experience health effects."
                )
                5 -> Triple(
                    "Very Poor",
                    "Stay indoors",
                    "Health warnings of emergency conditions. The entire population is likely to be affected."
                )
                else -> Triple(
                    "Unknown",
                    "Check air quality updates",
                    "Unable to determine air quality status."
                )
            }

            return AirQualityUiState(
                aqi = airQuality.aqi,
                pm25 = airQuality.pm25,
                pm10 = airQuality.pm10,
                status = status,
                advice = advice,
                healthMessage = healthMessage
            )
        }
    }
} 
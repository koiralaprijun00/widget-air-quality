package com.example.nepalweatherwidget.data.remote.model

import com.google.gson.annotations.SerializedName

data class AirQualityResponse(
    @SerializedName("list")
    val list: List<AirQualityData>
) {
    data class AirQualityData(
        @SerializedName("main")
        val main: Main,
        @SerializedName("components")
        val components: Components,
        @SerializedName("dt")
        val timestamp: Long
    ) {
        data class Main(
            @SerializedName("aqi")
            val aqi: Int
        )

        data class Components(
            @SerializedName("pm2_5")
            val pm25: Double,
            @SerializedName("pm10")
            val pm10: Double
        )
    }
} 
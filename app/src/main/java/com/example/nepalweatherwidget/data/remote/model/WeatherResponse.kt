package com.example.nepalweatherwidget.data.remote.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main")
    val main: Main,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("dt")
    val timestamp: Long
) {
    data class Main(
        @SerializedName("temp")
        val temp: Double,
        @SerializedName("feels_like")
        val feelsLike: Double,
        @SerializedName("humidity")
        val humidity: Int
    )

    data class Weather(
        @SerializedName("description")
        val description: String,
        @SerializedName("icon")
        val icon: String
    )

    data class Wind(
        @SerializedName("speed")
        val speed: Double
    )
} 
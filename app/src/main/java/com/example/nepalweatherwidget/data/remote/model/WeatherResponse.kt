package com.example.nepalweatherwidget.data.remote.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("coord")
    val coordinates: Coordinates,
    @SerializedName("weather")
    val weather: List<Weather>,
    @SerializedName("main")
    val main: Main,
    @SerializedName("wind")
    val wind: Wind,
    @SerializedName("name")
    val name: String,
    @SerializedName("dt")
    val timestamp: Long,
    @SerializedName("sys")
    val sys: Sys,
    @SerializedName("visibility")
    val visibility: Int,
    @SerializedName("clouds")
    val clouds: Clouds
) {
    data class Coordinates(
        @SerializedName("lat")
        val latitude: Double,
        @SerializedName("lon")
        val longitude: Double
    )

    data class Weather(
        @SerializedName("id")
        val id: Int,
        @SerializedName("main")
        val main: String,
        @SerializedName("description")
        val description: String,
        @SerializedName("icon")
        val icon: String
    )

    data class Main(
        @SerializedName("temp")
        val temp: Double,
        @SerializedName("feels_like")
        val feelsLike: Double,
        @SerializedName("temp_min")
        val tempMin: Double,
        @SerializedName("temp_max")
        val tempMax: Double,
        @SerializedName("pressure")
        val pressure: Int,
        @SerializedName("humidity")
        val humidity: Int
    )

    data class Wind(
        @SerializedName("speed")
        val speed: Double,
        @SerializedName("deg")
        val degree: Int
    )

    data class Sys(
        @SerializedName("sunrise")
        val sunrise: Long,
        @SerializedName("sunset")
        val sunset: Long
    )

    data class Clouds(
        @SerializedName("all")
        val all: Int
    )
} 
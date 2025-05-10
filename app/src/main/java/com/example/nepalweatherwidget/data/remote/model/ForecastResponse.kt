package com.example.nepalweatherwidget.data.remote.model

import com.google.gson.annotations.SerializedName

data class ForecastResponse(
    @SerializedName("list")
    val list: List<ForecastItem>,
    @SerializedName("city")
    val city: City
) {
    data class ForecastItem(
        @SerializedName("dt")
        val dt: Long,
        @SerializedName("main")
        val main: WeatherResponse.Main,
        @SerializedName("weather")
        val weather: List<WeatherResponse.Weather>,
        @SerializedName("wind")
        val wind: WeatherResponse.Wind
    )

    data class City(
        @SerializedName("id")
        val id: Int,
        @SerializedName("name")
        val name: String,
        @SerializedName("coord")
        val coord: WeatherResponse.Coordinates
    )
} 
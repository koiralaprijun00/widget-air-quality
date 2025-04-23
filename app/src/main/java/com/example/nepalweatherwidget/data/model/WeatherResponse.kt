package com.example.nepalweatherwidget.data.model

data class WeatherResponse(
    val main: Main,
    val weather: List<Weather>,
    val wind: Wind,
    val name: String
) {
    data class Main(
        val temp: Double,
        val humidity: Int
    )

    data class Weather(
        val description: String
    )

    data class Wind(
        val speed: Double
    )
}

fun WeatherResponse.toWeatherData(): WeatherData = WeatherData(
    location = name,
    temperature = main.temp,
    description = weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "Unknown",
    humidity = main.humidity,
    windSpeed = wind.speed
) 
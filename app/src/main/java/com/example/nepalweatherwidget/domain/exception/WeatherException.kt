package com.example.nepalweatherwidget.domain.exception

sealed class WeatherException : Exception() {
    data class NetworkError(override val message: String) : WeatherException()
    data class ApiError(override val message: String, val code: Int) : WeatherException()
    data class LocationError(override val message: String) : WeatherException()
    data class DataError(override val message: String) : WeatherException()
    data class UnknownError(override val message: String) : WeatherException()
} 
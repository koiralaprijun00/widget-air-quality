package com.example.nepalweatherwidget.core.error

sealed class WeatherException : Exception() {
    // Network-related errors
    sealed class NetworkException : WeatherException() {
        object NoInternet : NetworkException() {
            override val message = "No internet connection available"
        }
        data class Timeout(override val message: String = "Request timed out") : NetworkException()
        data class UnknownHost(override val message: String = "Unable to reach server") : NetworkException()
    }
    
    // API-related errors
    sealed class ApiException : WeatherException() {
        data class HttpError(val code: Int, override val message: String) : ApiException()
        data class InvalidApiKey(override val message: String = "Invalid API key") : ApiException()
        data class RateLimitExceeded(override val message: String = "API rate limit exceeded") : ApiException()
        data class ServerError(override val message: String = "Server error occurred") : ApiException()
    }
    
    // Location-related errors
    sealed class LocationException : WeatherException() {
        object PermissionDenied : LocationException() {
            override val message = "Location permission denied"
        }
        object LocationDisabled : LocationException() {
            override val message = "Location services are disabled"
        }
        data class LocationNotFound(override val message: String = "Location not found") : LocationException()
    }
    
    // Data-related errors
    sealed class DataException : WeatherException() {
        object NoDataAvailable : DataException() {
            override val message = "No weather data available"
        }
        data class InvalidData(override val message: String = "Invalid data format") : DataException()
        data class ParseError(override val message: String = "Failed to parse data") : DataException()
    }
    
    // General errors
    data class UnknownError(override val message: String = "An unknown error occurred") : WeatherException()
} 
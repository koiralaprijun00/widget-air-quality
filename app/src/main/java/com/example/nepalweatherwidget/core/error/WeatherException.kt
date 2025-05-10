package com.example.nepalweatherwidget.core.error

sealed class WeatherException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {
    
    // Network-related errors
    sealed class NetworkException(message: String, cause: Throwable? = null) : WeatherException(message, cause) {
        object NoInternet : NetworkException("No internet connection available")
        data class Timeout(override val message: String = "Request timed out") : NetworkException(message)
        data class UnknownHost(override val message: String = "Unable to reach server") : NetworkException(message)
    }
    
    // API-related errors
    sealed class ApiException(message: String, cause: Throwable? = null) : WeatherException(message, cause) {
        data class HttpError(val code: Int, override val message: String) : ApiException(message)
        data class InvalidApiKey(override val message: String = "Invalid API key") : ApiException(message)
        data class RateLimitExceeded(override val message: String = "API rate limit exceeded") : ApiException(message)
        data class ServerError(override val message: String = "Server error occurred") : ApiException(message)
    }
    
    // Location-related errors
    sealed class LocationException(message: String, cause: Throwable? = null) : WeatherException(message, cause) {
        object PermissionDenied : LocationException("Location permission denied")
        object LocationDisabled : LocationException("Location services are disabled")
        data class LocationNotFound(val location: String) : LocationException("Location not found: $location")
    }
    
    // Data-related errors
    sealed class DataException(message: String, cause: Throwable? = null) : WeatherException(message, cause) {
        object NoDataAvailable : DataException("No weather data available")
        data class InvalidData(override val message: String = "Invalid data format") : DataException(message)
        data class ParseError(override val message: String = "Failed to parse data") : DataException(message)
    }
    
    // General errors
    data class UnknownError(
        override val message: String = "An unknown error occurred",
        override val cause: Throwable? = null
    ) : WeatherException(message, cause)
} 
package com.example.nepalweatherwidget.core.error

import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.core.util.Logger
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorHandler @Inject constructor() {
    
    fun <T> handleError(throwable: Throwable): Result.Error {
        val exception = when (throwable) {
            is IOException -> handleNetworkError(throwable)
            is HttpException -> handleHttpError(throwable)
            is IllegalArgumentException -> WeatherException.DataException.InvalidData(throwable.message ?: "Invalid data")
            is WeatherException -> throwable  // Already a WeatherException
            else -> WeatherException.UnknownError(throwable.message ?: "Unknown error occurred")
        }
        
        // Log error for debugging
        Logger.e("Error handled: ${exception.message}", throwable)
        
        return Result.Error(exception)
    }
    
    private fun handleNetworkError(error: IOException): WeatherException.NetworkException {
        return when (error) {
            is UnknownHostException -> WeatherException.NetworkException.UnknownHost()
            is SocketTimeoutException -> WeatherException.NetworkException.Timeout()
            is ConnectException -> WeatherException.NetworkException.NoInternet
            else -> WeatherException.NetworkException.NoInternet
        }
    }
    
    private fun handleHttpError(error: HttpException): WeatherException {
        return when (error.code()) {
            401 -> WeatherException.ApiException.InvalidApiKey()
            403 -> WeatherException.ApiException.InvalidApiKey("Access forbidden")
            404 -> WeatherException.DataException.NoDataAvailable
            429 -> WeatherException.ApiException.RateLimitExceeded()
            in 500..599 -> WeatherException.ApiException.ServerError()
            else -> WeatherException.ApiException.HttpError(
                code = error.code(),
                message = error.message() ?: "HTTP error ${error.code()}"
            )
        }
    }
} 
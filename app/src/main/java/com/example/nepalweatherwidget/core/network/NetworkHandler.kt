package com.example.nepalweatherwidget.core.network

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.result.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkHandler @Inject constructor(
    private val networkMonitor: NetworkMonitor,
    private val errorHandler: ErrorHandler
) {
    suspend fun <T> safeApiCall(
        apiCall: suspend () -> T
    ): Result<T> {
        return try {
            if (!networkMonitor.isNetworkAvailable()) {
                Result.Error(WeatherException.NetworkException.NoInternet)
            } else {
                Result.Success(apiCall())
            }
        } catch (e: Exception) {
            errorHandler.handleError(e)
        }
    }
} 
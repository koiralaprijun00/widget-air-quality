package com.example.nepalweatherwidget.data.util

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val message: String, val cause: Throwable? = null) : Result<Nothing>()
    object Loading : Result<Nothing>()

    companion object {
        suspend fun <T> safeApiCall(
            apiCall: suspend () -> T
        ): Result<T> = try {
            Success(apiCall())
        } catch (e: Exception) {
            Error(e.message ?: "An unknown error occurred", e)
        }
    }
} 
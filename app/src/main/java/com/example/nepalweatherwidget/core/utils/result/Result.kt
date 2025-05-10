package com.example.nepalweatherwidget.core.result

import com.example.nepalweatherwidget.core.error.WeatherException

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val exception: WeatherException) : Result<Nothing>()
    
    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }
    
    inline fun onError(action: (WeatherException) -> Unit): Result<T> {
        if (this is Error) action(exception)
        return this
    }
    
    inline fun <R> map(transform: (T) -> R): Result<R> {
        return when (this) {
            is Success -> Success(transform(data))
            is Error -> Error(exception)
        }
    }
    
    inline fun <R> flatMap(transform: (T) -> Result<R>): Result<R> {
        return when (this) {
            is Success -> transform(data)
            is Error -> Error(exception)
        }
    }
} 
package com.example.nepalweatherwidget.domain.model

sealed class WidgetData {
    data class Success(
        val weather: WeatherData,
        val airQuality: AirQuality
    ) : WidgetData()
    
    data class Error(val message: String) : WidgetData()
    
    object Loading : WidgetData()
} 
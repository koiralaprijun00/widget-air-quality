package com.example.nepalweatherwidget.ui.dashboard

import com.example.nepalweatherwidget.domain.model.AirQualityData
import com.example.nepalweatherwidget.domain.model.WeatherData

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val weather: WeatherData,
        val airQuality: AirQualityData
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
} 
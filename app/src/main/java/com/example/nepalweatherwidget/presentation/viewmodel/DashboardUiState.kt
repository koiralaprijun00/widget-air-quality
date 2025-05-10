package com.example.nepalweatherwidget.presentation.viewmodel

import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.presentation.model.AirQualityUiState

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val weather: WeatherData,
        val airQuality: AirQualityUiState
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
} 
package com.example.nepalweatherwidget.features.dashboard.presentation

import com.example.nepalweatherwidget.features.airquality.presentation.AirQualityUiState
import com.example.nepalweatherwidget.features.weather.presentation.WeatherUiState

data class DashboardUiState(
    val weatherState: WeatherUiState = WeatherUiState(),
    val airQualityState: AirQualityUiState = AirQualityUiState(),
    val isLoading: Boolean = false,
    val error: String? = null
) 
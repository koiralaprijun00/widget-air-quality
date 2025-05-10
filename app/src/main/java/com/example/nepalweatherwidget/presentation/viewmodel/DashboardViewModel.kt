package com.example.nepalweatherwidget.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.domain.exception.WeatherException
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.presentation.model.AirQualityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val weather: WeatherData,
        val airQuality: AirQualityUiState
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    fun loadWeatherData(location: String) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            weatherRepository.getWeatherAndAirQuality(location)
                .onSuccess { (weather, airQuality) ->
                    _uiState.value = DashboardUiState.Success(
                        weather = weather,
                        airQuality = AirQualityUiState.fromAirQuality(airQuality)
                    )
                }
                .onFailure { exception ->
                    _uiState.value = DashboardUiState.Error(
                        when (exception) {
                            is WeatherException.NetworkError -> "No internet connection. Please check your network settings."
                            is WeatherException.ApiError -> "Server error: ${exception.message}"
                            is WeatherException.LocationError -> "Location not found. Please try a different location."
                            is WeatherException.DataError -> "Unable to fetch weather data: ${exception.message}"
                            else -> "An unexpected error occurred. Please try again later."
                        }
                    )
                }
        }
    }

    fun refreshData(location: String) {
        loadWeatherData(location)
    }

    fun retryLastOperation(location: String) {
        loadWeatherData(location)
    }
} 
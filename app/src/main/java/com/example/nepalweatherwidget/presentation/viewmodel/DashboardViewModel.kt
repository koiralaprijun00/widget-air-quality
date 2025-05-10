package com.example.nepalweatherwidget.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.presentation.model.AirQualityUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val weather: WeatherData,
        val airQuality: AirQualityUiState
    ) : DashboardUiState()
    data class Error(
        val message: String,
        val exception: WeatherException? = null,
        val canRetry: Boolean = true
    ) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private var lastLocation: String? = null

    fun loadWeatherData(location: String) {
        lastLocation = location
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            when (val result = weatherRepository.getWeatherAndAirQualityByLocationName(location)) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    _uiState.value = DashboardUiState.Success(
                        weather = weather,
                        airQuality = AirQualityUiState.fromAirQuality(airQuality)
                    )
                }
                is Result.Error -> {
                    _uiState.value = DashboardUiState.Error(
                        message = result.exception.message ?: "Unknown error",
                        exception = result.exception,
                        canRetry = result.exception !is WeatherException.ApiException.InvalidApiKey
                    )
                }
            }
        }
    }
    
    fun refreshData() {
        lastLocation?.let { loadWeatherData(it) }
    }
    
    fun retryLastOperation() {
        lastLocation?.let { loadWeatherData(it) }
    }
} 
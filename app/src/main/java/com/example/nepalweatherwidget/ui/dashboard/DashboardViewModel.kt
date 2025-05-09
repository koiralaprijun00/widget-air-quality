package com.example.nepalweatherwidget.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    fun loadWeatherData(location: String) {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            try {
                val result = weatherRepository.getWeatherAndAirQuality(location)
                result.fold(
                    onSuccess = { (weather, airQuality) ->
                        _uiState.value = DashboardUiState.Success(weather, airQuality)
                    },
                    onFailure = { error ->
                        _uiState.value = DashboardUiState.Error(error.message ?: "Unknown error")
                    }
                )
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun retry(location: String) {
        loadWeatherData(location)
    }
}
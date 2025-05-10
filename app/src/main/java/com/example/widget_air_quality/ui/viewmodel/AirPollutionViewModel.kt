package com.example.widget_air_quality.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widget_air_quality.data.model.AirPollutionResponse
import com.example.widget_air_quality.data.repository.AirPollutionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AirPollutionUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentAirPollution: AirPollutionResponse? = null,
    val forecastAirPollution: AirPollutionResponse? = null
)

@HiltViewModel
class AirPollutionViewModel @Inject constructor(
    private val repository: AirPollutionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AirPollutionUiState())
    val uiState: StateFlow<AirPollutionUiState> = _uiState.asStateFlow()

    fun fetchCurrentAirPollution(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getCurrentAirPollution(lat, lon)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        currentAirPollution = response
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
        }
    }

    fun fetchAirPollutionForecast(lat: Double, lon: Double) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            repository.getAirPollutionForecast(lat, lon)
                .onSuccess { response ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        forecastAirPollution = response
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = error.message ?: "Unknown error occurred"
                    )
                }
        }
    }
} 
package com.example.widget_air_quality.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.widget_air_quality.data.model.WeatherData
import com.example.widget_air_quality.data.repository.WeatherRepository
import com.example.widget_air_quality.data.repository.LocationRepository
import com.example.widget_air_quality.data.model.Location
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardUiState(
    val weatherData: WeatherData? = null,
    val location: Location? = null,
    val locationName: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadLastKnownLocation()
    }

    private fun loadLastKnownLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val location = locationRepository.getLastKnownLocation()
                if (location != null) {
                    _uiState.update { it.copy(location = location) }
                    fetchWeatherData(location.latitude, location.longitude)
                } else {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = "No location available. Please enable location services."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to get location: ${e.message}"
                    )
                }
            }
        }
    }

    fun fetchWeatherData(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                weatherRepository.getWeatherByCoordinates(latitude, longitude)
                    .onSuccess { weatherData ->
                        _uiState.update { 
                            it.copy(
                                weatherData = weatherData,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to fetch weather data: ${error.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to fetch weather data: ${e.message}"
                    )
                }
            }
        }
    }

    fun fetchWeatherByLocationName(locationName: String) {
        if (locationName.isBlank()) {
            _uiState.update { 
                it.copy(
                    error = "Please enter a location name",
                    locationName = locationName
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isLoading = true,
                    error = null,
                    locationName = locationName
                )
            }
            
            try {
                weatherRepository.getWeatherByLocationName(locationName)
                    .onSuccess { weatherData ->
                        _uiState.update { 
                            it.copy(
                                weatherData = weatherData,
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    .onFailure { error ->
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = "Failed to fetch weather data: ${error.message}"
                            )
                        }
                    }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = "Failed to fetch weather data: ${e.message}"
                    )
                }
            }
        }
    }

    fun updateLocationName(locationName: String) {
        _uiState.update { it.copy(locationName = locationName) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
} 
package com.example.nepalweatherwidget.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.result.Result
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
    data class Error(
        val message: String,
        val canRetry: Boolean = true,
        val exception: WeatherException? = null
    ) : DashboardUiState()
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var lastLocation: String = "Kathmandu"

    fun loadWeatherData(locationName: String) {
        lastLocation = locationName
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            weatherRepository.getWeatherAndAirQualityByLocationName(locationName)
                .onSuccess { (weather, airQuality) ->
                    _uiState.value = DashboardUiState.Success(
                        weather = weather,
                        airQuality = AirQualityUiState.fromAirQuality(airQuality)
                    )
                }
                .onError { exception ->
                    _uiState.value = DashboardUiState.Error(
                        message = getErrorMessage(exception),
                        canRetry = canRetryError(exception),
                        exception = exception
                    )
                }
        }
    }

    fun refreshData() {
        loadWeatherData(lastLocation)
    }

    fun retryLastOperation() {
        loadWeatherData(lastLocation)
    }

    private fun getErrorMessage(exception: WeatherException): String {
        return when (exception) {
            is WeatherException.NetworkException.NoInternet -> "No internet connection. Please check your network settings."
            is WeatherException.NetworkException.Timeout -> "Request timed out. Please try again."
            is WeatherException.NetworkException.UnknownHost -> "Unable to reach the server. Please try again later."
            is WeatherException.ApiException.InvalidApiKey -> "Invalid API configuration. Please contact support."
            is WeatherException.ApiException.RateLimitExceeded -> "Too many requests. Please try again later."
            is WeatherException.ApiException.ServerError -> "Server error occurred. Please try again later."
            is WeatherException.ApiException.HttpError -> "Error: ${exception.message}"
            is WeatherException.LocationException.PermissionDenied -> "Location permission is required to show weather data."
            is WeatherException.LocationException.LocationDisabled -> "Please enable location services to get weather updates."
            is WeatherException.LocationException.LocationNotFound -> "Location not found. Please try a different location."
            is WeatherException.DataException.NoDataAvailable -> "No weather data available for this location."
            is WeatherException.DataException.InvalidData -> "Unable to process weather data. Please try again."
            is WeatherException.DataException.ParseError -> "Error processing data. Please try again."
            is WeatherException.UnknownError -> "An unexpected error occurred. Please try again."
        }
    }

    private fun canRetryError(exception: WeatherException): Boolean {
        return when (exception) {
            is WeatherException.NetworkException,
            is WeatherException.ApiException.ServerError,
            is WeatherException.DataException.ParseError -> true
            else -> false
        }
    }
} 
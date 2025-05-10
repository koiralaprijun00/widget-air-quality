package com.example.nepalweatherwidget.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.network.NetworkMonitor
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.locations.domain.model.LocationItem
import com.example.nepalweatherwidget.features.locations.domain.usecase.GetOtherLocationsWeatherUseCase
import com.example.nepalweatherwidget.features.weather.domain.model.ForecastItem
import com.example.nepalweatherwidget.features.weather.domain.model.WeatherData
import com.example.nepalweatherwidget.features.weather.domain.usecase.GetForecastUseCase
import com.example.nepalweatherwidget.features.weather.domain.usecase.GetWeatherWithAirQualityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class DashboardUiState {
    object Loading : DashboardUiState()
    data class Success(
        val weather: WeatherData,
        val forecast: List<ForecastItem> = emptyList(),
        val otherLocations: List<LocationItem> = emptyList()
    ) : DashboardUiState()
    data class Error(
        val message: String,
        val exception: WeatherException? = null,
        val canRetry: Boolean = true
    ) : DashboardUiState()
}

@HiltViewModel
@ViewModelScoped
class DashboardViewModel @Inject constructor(
    private val getWeatherWithAirQualityUseCase: GetWeatherWithAirQualityUseCase,
    private val getForecastUseCase: GetForecastUseCase,
    private val getOtherLocationsWeatherUseCase: GetOtherLocationsWeatherUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {
    
    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private val _errorEvent = Channel<String>()
    val errorEvent = _errorEvent.receiveAsFlow()
    
    val isOffline = networkMonitor.isOnline
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = true
        )
    
    private var lastLocation: String? = null
    private var lastCoordinates: Pair<Double, Double>? = null

    fun loadWeatherData(location: String) {
        lastLocation = location
        lastCoordinates = null
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            // Get weather and air quality
            when (val result = getWeatherWithAirQualityUseCase(location)) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    
                    // Get forecast
                    val forecastResult = getForecastUseCase(location)
                    val forecast = when (forecastResult) {
                        is Result.Success -> forecastResult.data
                        is Result.Error -> emptyList()
                    }
                    
                    // Get other locations
                    val otherLocationsResult = getOtherLocationsWeatherUseCase(
                        listOf("Pokhara", "Lalitpur", "Bhaktapur", "Chitwan")
                    )
                    val otherLocations = when (otherLocationsResult) {
                        is Result.Success -> otherLocationsResult.data
                        is Result.Error -> emptyList()
                    }
                    
                    _uiState.value = DashboardUiState.Success(
                        weather = weather,
                        forecast = forecast,
                        otherLocations = otherLocations
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
    
    fun loadWeatherDataByCoordinates(latitude: Double, longitude: Double) {
        lastCoordinates = Pair(latitude, longitude)
        lastLocation = null
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            
            // Get location name from coordinates
            val locationResult = getWeatherWithAirQualityUseCase("$latitude,$longitude")
            when (locationResult) {
                is Result.Success -> {
                    val (weather, airQuality) = locationResult.data
                    
                    // Get forecast
                    val forecastResult = getForecastUseCase("$latitude,$longitude")
                    val forecast = when (forecastResult) {
                        is Result.Success -> forecastResult.data
                        is Result.Error -> emptyList()
                    }
                    
                    // Get other locations
                    val otherLocationsResult = getOtherLocationsWeatherUseCase(
                        listOf("Pokhara", "Lalitpur", "Bhaktapur", "Chitwan")
                    )
                    val otherLocations = when (otherLocationsResult) {
                        is Result.Success -> otherLocationsResult.data
                        is Result.Error -> emptyList()
                    }
                    
                    _uiState.value = DashboardUiState.Success(
                        weather = weather,
                        forecast = forecast,
                        otherLocations = otherLocations
                    )
                }
                is Result.Error -> {
                    _uiState.value = DashboardUiState.Error(
                        message = locationResult.exception.message ?: "Unknown error",
                        exception = locationResult.exception,
                        canRetry = locationResult.exception !is WeatherException.ApiException.InvalidApiKey
                    )
                }
            }
        }
    }
    
    fun refreshData() {
        when {
            lastLocation != null -> loadWeatherData(lastLocation!!)
            lastCoordinates != null -> loadWeatherDataByCoordinates(
                lastCoordinates!!.first,
                lastCoordinates!!.second
            )
        }
    }
    
    fun retryLastOperation() {
        refreshData()
    }
    
    fun onForecastItemClicked(forecastItem: ForecastItem) {
        // TODO: Implement forecast item click
    }
} 
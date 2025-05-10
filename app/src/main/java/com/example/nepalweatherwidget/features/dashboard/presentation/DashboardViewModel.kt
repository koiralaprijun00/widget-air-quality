package com.example.nepalweatherwidget.features.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.network.NetworkMonitor
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.dashboard.domain.model.ForecastItem
import com.example.nepalweatherwidget.features.dashboard.domain.model.Location
import com.example.nepalweatherwidget.features.dashboard.domain.model.LocationItem
import com.example.nepalweatherwidget.features.dashboard.domain.model.WeatherData
import com.example.nepalweatherwidget.features.dashboard.domain.repository.GeocodingRepository
import com.example.nepalweatherwidget.features.dashboard.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.features.dashboard.presentation.model.AirQualityUiState
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
    private val weatherRepository: WeatherRepository,
    private val geocodingRepository: GeocodingRepository,
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
            
            when (val result = weatherRepository.getWeatherAndAirQuality(location)) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    _uiState.value = DashboardUiState.Success(
                        weather = weather,
                        forecast = getMockForecast(), // TODO: Implement real forecast
                        otherLocations = getMockOtherLocations() // TODO: Implement real other locations
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
            
            when (val result = weatherRepository.getWeatherAndAirQualityByCoordinates(latitude, longitude)) {
                is Result.Success -> {
                    val (weather, airQuality) = result.data
                    _uiState.value = DashboardUiState.Success(
                        weather = weather,
                        forecast = getMockForecast(), // TODO: Implement real forecast
                        otherLocations = getMockOtherLocations() // TODO: Implement real other locations
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
    
    // Mock data for now
    private fun getMockForecast(): List<ForecastItem> {
        return listOf(
            ForecastItem("9:00", 22.0, 45, "😊", android.R.drawable.ic_dialog_info),
            ForecastItem("12:00", 25.0, 65, "😐", android.R.drawable.ic_dialog_info),
            ForecastItem("15:00", 26.0, 85, "😷", android.R.drawable.ic_dialog_info),
            ForecastItem("18:00", 23.0, 72, "😷", android.R.drawable.ic_dialog_info),
            ForecastItem("21:00", 20.0, 55, "😐", android.R.drawable.ic_dialog_info)
        )
    }
    
    private fun getMockOtherLocations(): List<LocationItem> {
        return listOf(
            LocationItem("Pokhara", 20.0, "Gandaki"),
            LocationItem("Lalitpur", 22.0, "Bagmati"),
            LocationItem("Bhaktapur", 21.0, "Bagmati"),
            LocationItem("Chitwan", 28.0, "Narayani")
        )
    }
} 
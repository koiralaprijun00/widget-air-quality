package com.example.nepalweatherwidget.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.data.model.WeatherData
import com.example.nepalweatherwidget.data.model.AirPollutionData
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase
import com.example.nepalweatherwidget.domain.usecase.GetAirQualityUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val getWeatherUseCase: GetWeatherUseCase,
    private val getAirQualityUseCase: GetAirQualityUseCase
) : ViewModel() {

    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData

    private val _airQualityData = MutableLiveData<AirPollutionData>()
    val airQualityData: LiveData<AirPollutionData> = _airQualityData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadWeatherData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val weather = getWeatherUseCase.getCurrentWeather(lat, lon)
                _weatherData.value = weather
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load weather data"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadAirQualityData(lat: Double, lon: Double) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val airQuality = getAirQualityUseCase.getCurrentAirQuality(lat, lon)
                _airQualityData.value = airQuality
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load air quality data"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
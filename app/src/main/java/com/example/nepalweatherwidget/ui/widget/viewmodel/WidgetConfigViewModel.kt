package com.example.nepalweatherwidget.ui.widget.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.Location
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.WidgetPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val widgetPreferencesRepository: WidgetPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        loadSavedLocations()
    }

    private fun loadSavedLocations() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            when (val result = weatherRepository.getSavedLocations()) {
                is Result.Success -> {
                    _uiState.value = UiState.Success(result.data)
                }
                is Result.Failure -> {
                    _uiState.value = UiState.Error(result.exception.message ?: "Failed to load locations")
                }
            }
        }
    }

    fun saveWidgetConfiguration(widgetId: Int, locationName: String, refreshInterval: String) {
        viewModelScope.launch {
            try {
                widgetPreferencesRepository.saveWidgetPreferences(
                    widgetId = widgetId,
                    locationName = locationName,
                    refreshInterval = refreshInterval
                )
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Failed to save widget configuration")
            }
        }
    }

    sealed class UiState {
        object Loading : UiState()
        data class Success(val locations: List<Location>) : UiState()
        data class Error(val message: String) : UiState()
    }
} 
package com.example.nepalweatherwidget.presentation.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.databinding.FragmentDashboardBinding
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.presentation.model.AirQualityUiState
import com.example.nepalweatherwidget.presentation.viewmodel.DashboardUiState
import com.example.nepalweatherwidget.presentation.viewmodel.DashboardViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    private var currentLocation: String = "Kathmandu, Nepal"

    companion object {
        private const val ARG_LOCATION = "location"
        
        fun newInstance(location: String? = null): DashboardFragment {
            return DashboardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_LOCATION, location)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.getString(ARG_LOCATION)?.let {
            currentLocation = it
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        observeViewModel()
        
        // Initial data load
        viewModel.loadWeatherData(currentLocation)
    }

    private fun setupViews() {
        binding.swipeRefresh.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DashboardUiState.Loading -> showLoading()
                        is DashboardUiState.Success -> {
                            showContent()
                            updateUI(state.weather, state.airQuality)
                        }
                        is DashboardUiState.Error -> showError(state)
                    }
                }
            }
        }
    }

    private fun updateUI(weather: WeatherData, airQuality: AirQualityUiState) {
        binding.apply {
            locationText.text = currentLocation
            temperatureText.text = getString(R.string.temperature_format, weather.temperature.toInt())
            descriptionText.text = weather.description
            humidityText.text = "Humidity: ${weather.humidity}%"
            windSpeedText.text = "Wind: ${weather.windSpeed} m/s"
            
            aqiText.text = "AQI: ${airQuality.aqi}"
            aqiDescription.text = airQuality.status
            pm25Text.text = "PM2.5: ${airQuality.pm25}"
            pm10Text.text = "PM10: ${airQuality.pm10}"
        }
    }

    private fun showLoading() {
        binding.loadingView.visibility = View.VISIBLE
        binding.swipeRefresh.isRefreshing = true
        binding.errorView.visibility = View.GONE
    }

    private fun showContent() {
        binding.loadingView.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        binding.errorView.visibility = View.GONE
    }

    private fun showError(errorState: DashboardUiState.Error) {
        binding.loadingView.visibility = View.GONE
        binding.swipeRefresh.isRefreshing = false
        binding.errorView.visibility = View.VISIBLE
        
        binding.errorText.text = errorState.message
        binding.retryButton.visibility = if (errorState.canRetry) View.VISIBLE else View.GONE
        
        binding.retryButton.setOnClickListener {
            viewModel.retryLastOperation()
        }
        
        // Show appropriate error icon based on exception type
        val errorIcon = when (errorState.exception) {
            is WeatherException.NetworkException -> R.drawable.ic_wifi_off
            else -> R.drawable.ic_error
        }
        binding.errorIcon.setImageResource(errorIcon)
        
        // Show snackbar for transient errors
        if (errorState.canRetry) {
            view?.let {
                Snackbar.make(it, errorState.message, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.retry)) { viewModel.retryLastOperation() }
                    .show()
            }
        }
    }
    
    fun updateLocation(location: String) {
        currentLocation = location
        viewModel.loadWeatherData(location)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
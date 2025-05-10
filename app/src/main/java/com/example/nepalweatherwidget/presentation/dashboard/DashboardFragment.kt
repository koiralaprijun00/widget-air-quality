package com.example.nepalweatherwidget.presentation.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.extension.setIntSafe
import com.example.nepalweatherwidget.core.extension.setPercentageSafe
import com.example.nepalweatherwidget.core.extension.setTemperatureSafe
import com.example.nepalweatherwidget.core.extension.setTextResourceSafe
import com.example.nepalweatherwidget.core.extension.setTextSafe
import com.example.nepalweatherwidget.core.extension.setVisibilitySafe
import com.example.nepalweatherwidget.core.monitor.NetworkMonitor
import com.example.nepalweatherwidget.core.util.Logger
import com.example.nepalweatherwidget.databinding.FragmentDashboardBinding
import com.example.nepalweatherwidget.presentation.model.AirQualityUiState
import com.example.nepalweatherwidget.presentation.viewmodel.DashboardUiState
import com.example.nepalweatherwidget.presentation.viewmodel.DashboardViewModel
import com.example.nepalweatherwidget.ui.LocationAdapter
import com.example.nepalweatherwidget.ui.ForecastAdapter
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var otherLocationsAdapter: LocationAdapter
    private var currentLocation: String = "Kathmandu, Nepal"
    
    @Inject
    lateinit var networkMonitor: NetworkMonitor

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
        Logger.d("DashboardFragment: onViewCreated")
        
        setupViews()
        setupSwipeRefresh()
        setupRecyclerViews()
        setupClickListeners()
        setupNetworkObserver()
        observeViewModel()
        
        // Initial data load
        viewModel.loadWeatherData(currentLocation)
    }

    private fun setupViews() {
        // Set initial state
        binding.loadingView.setVisibilitySafe(false)
        binding.contentView.setVisibilitySafe(false)
        binding.errorView.setVisibilitySafe(false)
        binding.offlineIndicator.setVisibilitySafe(false)
        
        binding.retryButton.setOnClickListener {
            Logger.d("DashboardFragment: Retry button clicked")
            viewModel.retryLastOperation()
        }
    }

    private fun setupNetworkObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline.collectLatest { isOnline ->
                    Logger.d("DashboardFragment: Network status changed - Online: $isOnline")
                    binding.offlineIndicator.setVisibilitySafe(!isOnline)
                    
                    // If we come back online, refresh the data
                    if (isOnline) {
                        viewModel.refreshData()
                    }
                }
            }
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            if (networkMonitor.isNetworkAvailable()) {
                Logger.d("DashboardFragment: Manual refresh triggered")
                viewModel.refreshData()
            } else {
                Logger.w("DashboardFragment: Refresh attempted while offline")
                showOfflineSnackbar()
                binding.swipeRefreshLayout.isRefreshing = false
            }
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.primary_dark,
            R.color.accent
        )
    }

    private fun showOfflineSnackbar() {
        Snackbar.make(
            binding.root,
            "You're offline. Please check your internet connection.",
            Snackbar.LENGTH_LONG
        ).setAction("Settings") {
            Logger.d("DashboardFragment: Opening network settings")
            startActivity(android.provider.Settings.ACTION_WIRELESS_SETTINGS)
        }.show()
    }

    private fun setupRecyclerViews() {
        forecastAdapter = ForecastAdapter(emptyList())
        otherLocationsAdapter = LocationAdapter { location ->
            if (networkMonitor.isNetworkAvailable()) {
                Logger.d("DashboardFragment: Location selected: ${location.name}")
                currentLocation = location
                viewModel.loadWeatherData(location)
            } else {
                Logger.w("DashboardFragment: Location change attempted while offline")
                showOfflineSnackbar()
            }
        }

        binding.forecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = forecastAdapter
        }

        binding.otherLocationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = otherLocationsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.aqiRangeCard.setOnClickListener {
            // TODO: Navigate to AQI range screen
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collectLatest { state ->
                    when (state) {
                        is DashboardUiState.Loading -> {
                            Logger.d("DashboardFragment: Loading state")
                            showLoading()
                        }
                        is DashboardUiState.Success -> {
                            Logger.d("DashboardFragment: Success state")
                            showContent()
                            updateUI(state.weather, state.airQuality)
                        }
                        is DashboardUiState.Error -> {
                            Logger.e("DashboardFragment: Error state - ${state.message}", state.exception)
                            showError(state)
                        }
                    }
                }
            }
        }
    }

    private fun updateUI(weather: WeatherData, airQuality: AirQualityUiState) {
        binding.apply {
            locationName.setTextSafe(currentLocation)
            locationSub.setTextResourceSafe(R.string.location_subtitle)
            weatherTemp.setTemperatureSafe(weather.temperature)
            weatherDesc.setTextSafe(weather.description)
            
            aqiValue.setIntSafe(airQuality.aqi)
            aqiLabel.setTextResourceSafe(airQuality.statusRes)
            aqiDesc.setTextResourceSafe(airQuality.adviceRes)
            healthAdviceText.setTextResourceSafe(airQuality.healthRes)
            
            // Update forecast and other locations if available
            // forecastAdapter.submitList(weather.forecast)
            // otherLocationsAdapter.submitList(airQuality.nearbyLocations)
        }
    }

    private fun showLoading() {
        binding.loadingView.setVisibilitySafe(true)
        binding.contentView.setVisibilitySafe(false)
        binding.errorView.setVisibilitySafe(false)
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun showContent() {
        binding.loadingView.setVisibilitySafe(false)
        binding.contentView.setVisibilitySafe(true)
        binding.errorView.setVisibilitySafe(false)
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun showError(errorState: DashboardUiState.Error) {
        binding.loadingView.setVisibilitySafe(false)
        binding.contentView.setVisibilitySafe(false)
        binding.errorView.setVisibilitySafe(true)
        binding.swipeRefreshLayout.isRefreshing = false
        
        binding.errorText.setTextSafe(errorState.message)
        binding.retryButton.setVisibilitySafe(errorState.canRetry)
        
        binding.retryButton.setOnClickListener {
            if (networkMonitor.isNetworkAvailable()) {
                viewModel.retryLastOperation()
            } else {
                showOfflineSnackbar()
            }
        }
        
        // Show appropriate error icon based on exception type
        val errorIcon = when (errorState.exception) {
            is WeatherException.NetworkException -> R.drawable.ic_wifi_off
            is WeatherException.LocationException -> R.drawable.ic_location_off
            else -> R.drawable.ic_error
        }
        binding.errorIcon.setImageResource(errorIcon)
        
        // Show snackbar for transient errors
        if (errorState.canRetry) {
            view?.let {
                Snackbar.make(it, errorState.message, Snackbar.LENGTH_LONG)
                    .setAction(getString(R.string.retry)) { 
                        if (networkMonitor.isNetworkAvailable()) {
                            viewModel.retryLastOperation()
                        } else {
                            showOfflineSnackbar()
                        }
                    }
                    .show()
            }
        }
    }
    
    fun updateLocation(location: String) {
        if (networkMonitor.isNetworkAvailable()) {
            currentLocation = location
            viewModel.loadWeatherData(location)
        } else {
            showOfflineSnackbar()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Logger.d("DashboardFragment: onDestroyView")
        _binding = null
    }
} 
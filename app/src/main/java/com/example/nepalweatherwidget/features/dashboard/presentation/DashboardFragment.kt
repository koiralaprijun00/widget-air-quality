package com.example.nepalweatherwidget.features.dashboard.presentation

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.databinding.FragmentDashboardBinding
import com.example.nepalweatherwidget.features.dashboard.domain.model.WeatherData
import com.example.nepalweatherwidget.core.location.LocationService
import com.example.nepalweatherwidget.features.dashboard.presentation.DashboardUiState
import com.example.nepalweatherwidget.features.dashboard.presentation.DashboardViewModel
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
    
    @Inject
    lateinit var locationService: LocationService
    
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var otherLocationsAdapter: LocationAdapter
    
    private var currentLocation: String = "Kathmandu"
    private var isUsingCurrentLocation = false
    private var hasShownLocationError = false
    private var locationErrorSnackbar: Snackbar? = null

    companion object {
        private const val ARG_LOCATION = "location"
        private const val KEY_HAS_SHOWN_LOCATION_ERROR = "has_shown_location_error"
        private const val KEY_CURRENT_LOCATION = "current_location"
        private const val KEY_IS_USING_CURRENT_LOCATION = "is_using_current_location"
        
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
        savedInstanceState?.let {
            hasShownLocationError = it.getBoolean(KEY_HAS_SHOWN_LOCATION_ERROR, false)
            currentLocation = it.getString(KEY_CURRENT_LOCATION, currentLocation)
            isUsingCurrentLocation = it.getBoolean(KEY_IS_USING_CURRENT_LOCATION, false)
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
        setupRecyclerViews()
        setupSwipeRefresh()
        observeViewModel()
        
        // Check if we should use current location
        if (arguments?.getString(ARG_LOCATION) == null && hasLocationPermission()) {
            loadCurrentLocationWeather()
        } else {
            viewModel.loadWeatherData(currentLocation)
        }
    }
    
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.apply {
            putBoolean(KEY_HAS_SHOWN_LOCATION_ERROR, hasShownLocationError)
            putString(KEY_CURRENT_LOCATION, currentLocation)
            putBoolean(KEY_IS_USING_CURRENT_LOCATION, isUsingCurrentLocation)
        }
    }

    private fun setupViews() {
        binding.apply {
            errorView.visibility = View.GONE
            loadingView.visibility = View.VISIBLE
            contentScrollView.visibility = View.GONE
            offlineIndicator.visibility = View.GONE
        }
    }

    private fun setupRecyclerViews() {
        forecastAdapter = ForecastAdapter { forecastItem ->
            viewModel.onForecastItemClicked(forecastItem)
        }
        
        otherLocationsAdapter = LocationAdapter { location ->
            currentLocation = location.name
            isUsingCurrentLocation = false
            viewModel.loadWeatherData(location.name)
        }

        binding.forecastRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = forecastAdapter
            setHasFixedSize(true)
        }

        binding.otherLocationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = otherLocationsAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.apply {
            setOnRefreshListener {
                if (isUsingCurrentLocation) {
                    loadCurrentLocationWeather()
                } else {
                    viewModel.refreshData()
                }
            }
            setColorSchemeResources(
                R.color.primary,
                R.color.primary_dark,
                R.color.accent
            )
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collectLatest { state ->
                        handleUiState(state)
                    }
                }
                
                launch {
                    viewModel.isOffline.collectLatest { isOffline ->
                        binding.offlineIndicator.visibility = if (isOffline) View.VISIBLE else View.GONE
                    }
                }
                
                launch {
                    viewModel.errorEvent.collectLatest { error ->
                        showErrorSnackbar(error)
                    }
                }
            }
        }
    }
    
    private fun handleUiState(state: DashboardUiState) {
        when (state) {
            is DashboardUiState.Loading -> showLoading()
            is DashboardUiState.Success -> {
                showContent()
                updateForecast(state.forecast)
                updateOtherLocations(state.otherLocations)
            }
            is DashboardUiState.Error -> showError(state)
        }
    }

    private fun showLoading() {
        binding.apply {
            loadingView.visibility = View.VISIBLE
            contentScrollView.visibility = View.GONE
            errorView.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = true
        }
    }

    private fun showContent() {
        binding.apply {
            loadingView.visibility = View.GONE
            contentScrollView.visibility = View.VISIBLE
            errorView.visibility = View.GONE
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun showError(errorState: DashboardUiState.Error) {
        binding.apply {
            loadingView.visibility = View.GONE
            contentScrollView.visibility = View.GONE
            errorView.visibility = View.VISIBLE
            swipeRefreshLayout.isRefreshing = false
            
            errorText.text = errorState.message
            retryButton.visibility = if (errorState.canRetry) View.VISIBLE else View.GONE
            
            retryButton.setOnClickListener {
                if (isUsingCurrentLocation) {
                    loadCurrentLocationWeather()
                } else {
                    viewModel.retryLastOperation()
                }
            }
            
            val errorIcon = when (errorState.exception) {
                is WeatherException.NetworkException -> R.drawable.ic_wifi_off
                is WeatherException.LocationException -> R.drawable.ic_location_off
                is WeatherException.ApiException -> R.drawable.ic_error
                else -> R.drawable.ic_error
            }
            errorIcon.setImageResource(errorIcon)
        }
    }

    private fun updateForecast(forecast: List<ForecastItem>) {
        forecastAdapter.submitList(forecast)
        binding.forecastSection.visibility = if (forecast.isEmpty()) View.GONE else View.VISIBLE
    }
    
    private fun updateOtherLocations(locations: List<LocationItem>) {
        otherLocationsAdapter.submitList(locations)
        binding.otherLocationsSection.visibility = if (locations.isEmpty()) View.GONE else View.VISIBLE
    }
    
    @SuppressLint("MissingPermission")
    private fun loadCurrentLocationWeather() {
        if (!hasLocationPermission()) {
            viewModel.loadWeatherData(currentLocation)
            return
        }
        
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val location = locationService.getLastLocation()
                if (location != null) {
                    isUsingCurrentLocation = true
                    viewModel.loadWeatherDataByCoordinates(location.latitude, location.longitude)
                } else {
                    // Fallback to default location
                    if (!hasShownLocationError) {
                        showLocationError("Unable to get current location")
                        hasShownLocationError = true
                    }
                    isUsingCurrentLocation = false
                    viewModel.loadWeatherData(currentLocation)
                }
            } catch (e: Exception) {
                if (!hasShownLocationError) {
                    showLocationError("Location error: ${e.message}")
                    hasShownLocationError = true
                }
                isUsingCurrentLocation = false
                viewModel.loadWeatherData(currentLocation)
            }
        }
    }
    
    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }
    
    fun onLocationPermissionGranted() {
        hasShownLocationError = false
        loadCurrentLocationWeather()
    }
    
    private fun showLocationError(message: String) {
        locationErrorSnackbar?.dismiss()
        locationErrorSnackbar = Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("Settings") {
                startActivity(Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.fromParts("package", requireContext().packageName, null)
                })
            }
        locationErrorSnackbar?.show()
    }
    
    private fun showErrorSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }
    
    fun updateLocation(location: String) {
        currentLocation = location
        isUsingCurrentLocation = false
        viewModel.loadWeatherData(location)
    }

    override fun onDestroyView() {
        locationErrorSnackbar?.dismiss()
        locationErrorSnackbar = null
        _binding = null
        super.onDestroyView()
    }
} 
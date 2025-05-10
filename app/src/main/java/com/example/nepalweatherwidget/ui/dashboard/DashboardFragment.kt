package com.example.nepalweatherwidget.ui.dashboard

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.databinding.FragmentDashboardBinding
import com.example.nepalweatherwidget.domain.model.ForecastItem
import com.example.nepalweatherwidget.domain.model.LocationItem
import com.example.nepalweatherwidget.ui.ForecastAdapter
import com.example.nepalweatherwidget.ui.LocationAdapter
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var otherLocationsAdapter: LocationAdapter
    private var currentLocation: String = "Kathmandu, Nepal"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews()
        setupSwipeRefresh()
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
    }

    private fun setupViews() {
        // Set initial state
        binding.loadingView.visibility = View.VISIBLE
        binding.contentView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadWeatherData(currentLocation)
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.primary_dark,
            R.color.accent
        )
    }

    private fun setupRecyclerViews() {
        forecastAdapter = ForecastAdapter()
        otherLocationsAdapter = LocationAdapter { location ->
            viewModel.loadWeatherData(location)
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
                viewModel.uiState.collect { state ->
                    when (state) {
                        is DashboardUiState.Loading -> showLoading()
                        is DashboardUiState.Success -> {
                            showContent()
                            updateUI(state.weather, state.airQuality)
                        }
                        is DashboardUiState.Error -> showError(state.message)
                    }
                }
            }
        }
    }

    private fun updateUI(weather: WeatherData, airQuality: AirQualityData) {
        binding.apply {
            locationName.text = currentLocation
            locationSub.text = getString(R.string.location_subtitle)
            weatherTemp.text = getString(R.string.temperature_format, weather.temperature.toInt())
            weatherDesc.text = weather.description
            
            aqiValue.text = airQuality.aqi.toString()
            aqiLabel.setText(airQuality.statusRes)
            aqiDesc.setText(airQuality.adviceRes)
            healthAdviceText.setText(airQuality.healthRes)
            
            forecastAdapter.submitList(weather.forecast)
            otherLocationsAdapter.submitList(airQuality.nearbyLocations)
        }
    }

    private fun showLoading() {
        binding.loadingView.visibility = View.VISIBLE
        binding.contentView.visibility = View.GONE
        binding.errorView.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun showContent() {
        binding.loadingView.visibility = View.GONE
        binding.contentView.visibility = View.VISIBLE
        binding.errorView.visibility = View.GONE
        binding.swipeRefreshLayout.isRefreshing = false
    }

    private fun showError(message: String) {
        binding.loadingView.visibility = View.GONE
        binding.contentView.visibility = View.GONE
        binding.errorView.visibility = View.VISIBLE
        binding.swipeRefreshLayout.isRefreshing = false
        
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.retry)) { viewModel.loadWeatherData(currentLocation) }
                .show()
        }
    }
} 
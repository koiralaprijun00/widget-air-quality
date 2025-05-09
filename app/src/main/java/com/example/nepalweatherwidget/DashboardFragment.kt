package com.example.nepalweatherwidget

import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.snackbar.Snackbar
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase
import com.example.nepalweatherwidget.ui.ForecastAdapter
import com.example.nepalweatherwidget.ui.LocationAdapter
import com.example.nepalweatherwidget.domain.model.ForecastItem
import com.example.nepalweatherwidget.domain.model.LocationItem
import androidx.core.content.ContextCompat
import androidx.annotation.ColorRes
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

class DashboardFragment : Fragment() {
    private lateinit var binding: FragmentDashboardBinding
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter
    private lateinit var otherLocationsAdapter: OtherLocationsAdapter
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
            updateDashboard(currentLocation)
        }
        binding.swipeRefreshLayout.setColorSchemeResources(
            R.color.primary,
            R.color.primary_dark,
            R.color.accent
        )
    }

    fun updateLocation(location: String) {
        currentLocation = location
        updateDashboard(location)
    }

    private fun updateDashboard(location: String) {
        showLoading()
        try {
            // Simulate network delay
            android.os.Handler(Looper.getMainLooper()).postDelayed({
                try {
                    val weatherData = GetWeatherUseCase.getMockWeatherData()
                    binding.locationName.text = location
                    binding.locationSub.text = getString(R.string.location_subtitle)
                    binding.weatherTemp.text = getString(R.string.temperature_format, weatherData.temperature.toInt())
                    binding.weatherDesc.text = weatherData.description
                    
                    val aqi = 75 // Example AQI value
                    binding.aqiValue.text = aqi.toString()
                    
                    val (statusRes, adviceRes, healthRes) = when (aqi) {
                        in 0..50 -> Triple(R.string.aqi_good, R.string.aqi_advice_unhealthy, R.string.aqi_health_good)
                        in 51..100 -> Triple(R.string.aqi_moderate, R.string.aqi_advice_unhealthy, R.string.aqi_health_moderate)
                        in 101..150 -> Triple(R.string.aqi_unhealthy_sensitive, R.string.aqi_advice_unhealthy, R.string.aqi_health_unhealthy_sensitive)
                        in 151..200 -> Triple(R.string.aqi_unhealthy, R.string.aqi_advice_unhealthy, R.string.aqi_health_unhealthy)
                        in 201..300 -> Triple(R.string.aqi_very_unhealthy, R.string.aqi_advice_unhealthy, R.string.aqi_health_very_unhealthy)
                        else -> Triple(R.string.aqi_hazardous, R.string.aqi_advice_unhealthy, R.string.aqi_health_hazardous)
                    }
                    
                    binding.aqiLabel.setText(statusRes)
                    binding.aqiDesc.setText(adviceRes)
                    binding.healthAdviceText.setText(healthRes)
                    
                    showContent()
                } catch (e: Exception) {
                    showError(e.message ?: getString(R.string.error_unknown))
                }
            }, 1000) // Simulate 1 second delay
        } catch (e: Exception) {
            showError(e.message ?: getString(R.string.error_unknown))
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
                .setAction(getString(R.string.retry)) { updateDashboard(currentLocation) }
                .show()
        }
    }

    private fun getColor(@ColorRes colorRes: Int): Int {
        return ContextCompat.getColor(requireContext(), colorRes)
    }
}
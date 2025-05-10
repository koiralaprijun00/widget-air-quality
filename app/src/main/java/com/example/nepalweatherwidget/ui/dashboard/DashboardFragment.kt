package com.example.nepalweatherwidget.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nepalweatherwidget.databinding.FragmentDashboardBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var otherLocationsAdapter: OtherLocationsAdapter

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
        setupRecyclerViews()
        setupClickListeners()
        observeViewModel()
        loadData()
    }

    private fun setupRecyclerViews() {
        otherLocationsAdapter = OtherLocationsAdapter { location ->
            // Handle location click
            viewModel.loadWeatherData(location.latitude, location.longitude)
            viewModel.loadAirQualityData(location.latitude, location.longitude)
        }

        binding.rvOtherLocations.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = otherLocationsAdapter
        }
    }

    private fun setupClickListeners() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
        }
    }

    private fun observeViewModel() {
        viewModel.weatherData.observe(viewLifecycleOwner) { weatherData ->
            binding.apply {
                tvTemperature.text = "${weatherData.temperature}°C"
                tvWeatherDescription.text = weatherData.description
                tvHumidity.text = "${weatherData.humidity}%"
                tvWindSpeed.text = "${weatherData.windSpeed} m/s"
            }
        }

        viewModel.airQualityData.observe(viewLifecycleOwner) { airQualityData ->
            binding.apply {
                tvAqiValue.text = airQualityData.aqi.toString()
                tvAqiDescription.text = airQualityData.description
                tvPm25Value.text = "${airQualityData.pm25} μg/m³"
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefresh.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            // Handle error state
            binding.tvError.text = error
            binding.tvError.visibility = if (error.isNotEmpty()) View.VISIBLE else View.GONE
        }
    }

    private fun loadData() {
        // Load data for Kathmandu by default
        viewModel.loadWeatherData(27.7172, 85.3240)
        viewModel.loadAirQualityData(27.7172, 85.3240)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 
package com.example.nepalweatherwidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase

class DashboardFragment : Fragment() {
    private var currentLocation: String = "Kathmandu, Nepal"
    private var locationText: TextView? = null
    private var currentTemp: TextView? = null
    private var weatherDescription: TextView? = null
    private var feelsLike: TextView? = null
    private var aqiValue: TextView? = null
    private var aqiStatus: TextView? = null
    private var aqiAdvice: TextView? = null
    private var aqiHealthMessage: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Get references to your views
        locationText = view.findViewById(R.id.locationText)
        currentTemp = view.findViewById(R.id.currentTemp)
        weatherDescription = view.findViewById(R.id.weatherDescription)
        feelsLike = view.findViewById(R.id.feelsLike)
        aqiValue = view.findViewById(R.id.aqiValue)
        aqiStatus = view.findViewById(R.id.aqiStatus)
        aqiAdvice = view.findViewById(R.id.aqiAdvice)
        aqiHealthMessage = view.findViewById(R.id.aqiHealthMessage)
        // Initial load
        updateDashboard(currentLocation)
    }

    fun updateLocation(location: String) {
        currentLocation = location
        updateDashboard(location)
    }

    private fun updateDashboard(location: String) {
        // For now, just use mock data and update the location
        locationText?.text = location
        val weatherData = GetWeatherUseCase.getMockWeatherData()
        currentTemp?.text = getString(R.string.temperature_format, weatherData.temperature.toInt())
        weatherDescription?.text = weatherData.description
        feelsLike?.text = getString(R.string.feels_like_22) // Replace with real feels like value if available
        val aqi = 75 // Example AQI value
        aqiValue?.text = aqi.toString()
        val (statusRes, adviceRes, healthRes) = when {
            aqi <= 50 -> Triple(R.string.aqi_good, R.string.aqi_advice_unhealthy, R.string.aqi_health_good)
            aqi <= 100 -> Triple(R.string.aqi_moderate, R.string.aqi_advice_unhealthy, R.string.aqi_health_moderate)
            aqi <= 150 -> Triple(R.string.aqi_unhealthy_sensitive, R.string.aqi_advice_unhealthy, R.string.aqi_health_unhealthy_sensitive)
            aqi <= 200 -> Triple(R.string.aqi_unhealthy, R.string.aqi_advice_unhealthy, R.string.aqi_health_unhealthy)
            aqi <= 300 -> Triple(R.string.aqi_very_unhealthy, R.string.aqi_advice_unhealthy, R.string.aqi_health_very_unhealthy)
            else -> Triple(R.string.aqi_hazardous, R.string.aqi_advice_unhealthy, R.string.aqi_health_hazardous)
        }
        aqiStatus?.setText(statusRes)
        aqiAdvice?.setText(adviceRes)
        aqiHealthMessage?.setText(healthRes)
    }
} 
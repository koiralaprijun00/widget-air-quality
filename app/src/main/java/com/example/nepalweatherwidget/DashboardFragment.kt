package com.example.nepalweatherwidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase

class DashboardFragment : Fragment() {
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
        val locationText = view.findViewById<TextView>(R.id.locationText)
        val currentTemp = view.findViewById<TextView>(R.id.currentTemp)
        val weatherDescription = view.findViewById<TextView>(R.id.weatherDescription)
        val feelsLike = view.findViewById<TextView>(R.id.feelsLike)
        val aqiValue = view.findViewById<TextView>(R.id.aqiValue)
        val aqiStatus = view.findViewById<TextView>(R.id.aqiStatus)
        val aqiAdvice = view.findViewById<TextView>(R.id.aqiAdvice)
        val aqiHealthMessage = view.findViewById<TextView>(R.id.aqiHealthMessage)

        // Get mock data
        val weatherData = GetWeatherUseCase.getMockWeatherData()
        // TODO: Replace with real AQI data source
        val aqi = 75 // Example AQI value

        // Set data
        locationText.text = weatherData.location
        currentTemp.text = getString(R.string.temperature_format, weatherData.temperature.toInt())
        weatherDescription.text = weatherData.description
        feelsLike.text = getString(R.string.feels_like_22) // Replace with real feels like value if available

        aqiValue.text = aqi.toString()
        val (statusRes, adviceRes, healthRes) = when {
            aqi <= 50 -> Triple(R.string.aqi_good, R.string.aqi_advice_unhealthy, R.string.aqi_health_good)
            aqi <= 100 -> Triple(R.string.aqi_moderate, R.string.aqi_advice_unhealthy, R.string.aqi_health_moderate)
            aqi <= 150 -> Triple(R.string.aqi_unhealthy_sensitive, R.string.aqi_advice_unhealthy, R.string.aqi_health_unhealthy_sensitive)
            aqi <= 200 -> Triple(R.string.aqi_unhealthy, R.string.aqi_advice_unhealthy, R.string.aqi_health_unhealthy)
            aqi <= 300 -> Triple(R.string.aqi_very_unhealthy, R.string.aqi_advice_unhealthy, R.string.aqi_health_very_unhealthy)
            else -> Triple(R.string.aqi_hazardous, R.string.aqi_advice_unhealthy, R.string.aqi_health_hazardous)
        }
        aqiStatus.setText(statusRes)
        aqiAdvice.setText(adviceRes)
        aqiHealthMessage.setText(healthRes)
    }
} 
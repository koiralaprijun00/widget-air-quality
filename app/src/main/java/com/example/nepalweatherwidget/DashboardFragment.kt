package com.example.nepalweatherwidget

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase
import com.google.android.material.snackbar.Snackbar

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
    private var swipeRefreshLayout: SwipeRefreshLayout? = null
    private var loadingView: View? = null
    private var contentView: View? = null
    private var errorView: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViews(view)
        setupSwipeRefresh()
        // Initial load
        updateDashboard(currentLocation)
    }

    private fun setupViews(view: View) {
        // Get references to your views
        locationText = view.findViewById(R.id.locationText)
        currentTemp = view.findViewById(R.id.currentTemp)
        weatherDescription = view.findViewById(R.id.weatherDescription)
        feelsLike = view.findViewById(R.id.feelsLike)
        aqiValue = view.findViewById(R.id.aqiValue)
        aqiStatus = view.findViewById(R.id.aqiStatus)
        aqiAdvice = view.findViewById(R.id.aqiAdvice)
        aqiHealthMessage = view.findViewById(R.id.aqiHealthMessage)
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        loadingView = view.findViewById(R.id.loadingView)
        contentView = view.findViewById(R.id.contentView)
        errorView = view.findViewById(R.id.errorView)

        view.findViewById<View>(R.id.retryButton)?.setOnClickListener {
            updateDashboard(currentLocation)
        }
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout?.setOnRefreshListener {
            updateDashboard(currentLocation)
        }
        swipeRefreshLayout?.setColorSchemeResources(
            R.color.colorPrimary,
            R.color.colorPrimaryDark,
            R.color.colorAccent
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
            android.os.Handler().postDelayed({
                try {
                    val weatherData = GetWeatherUseCase.getMockWeatherData()
                    locationText?.text = location
                    currentTemp?.text = getString(R.string.temperature_format, weatherData.temperature.toInt())
                    weatherDescription?.text = weatherData.description
                    feelsLike?.text = getString(R.string.feels_like_22)
                    
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
        loadingView?.visibility = View.VISIBLE
        contentView?.visibility = View.GONE
        errorView?.visibility = View.GONE
        swipeRefreshLayout?.isRefreshing = true
    }

    private fun showContent() {
        loadingView?.visibility = View.GONE
        contentView?.visibility = View.VISIBLE
        errorView?.visibility = View.GONE
        swipeRefreshLayout?.isRefreshing = false
    }

    private fun showError(message: String) {
        loadingView?.visibility = View.GONE
        contentView?.visibility = View.GONE
        errorView?.visibility = View.VISIBLE
        swipeRefreshLayout?.isRefreshing = false
        
        view?.let {
            Snackbar.make(it, message, Snackbar.LENGTH_LONG)
                .setAction("Retry") { updateDashboard(currentLocation) }
                .show()
        }
    }
}
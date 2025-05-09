package com.example.nepalweatherwidget

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase
import com.example.nepalweatherwidget.ui.ForecastAdapter
import com.example.nepalweatherwidget.ui.LocationAdapter
import com.example.nepalweatherwidget.domain.model.ForecastItem
import com.example.nepalweatherwidget.domain.model.LocationItem

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

        // 1st Section: Location & AQI
        val aqiValue = 185
        val aqiCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.aqiCard)
        val locationName = view.findViewById<TextView>(R.id.locationName)
        val locationSub = view.findViewById<TextView>(R.id.locationSub)
        val weatherIcon = view.findViewById<ImageView>(R.id.weatherIcon)
        val weatherTemp = view.findViewById<TextView>(R.id.weatherTemp)
        val weatherDesc = view.findViewById<TextView>(R.id.weatherDesc)
        val aqiValueText = view.findViewById<TextView>(R.id.aqiValue)
        val aqiLabel = view.findViewById<TextView>(R.id.aqiLabel)
        val aqiDesc = view.findViewById<TextView>(R.id.aqiDesc)

        // Set mock data
        locationName.text = "Kathmandu"
        locationSub.text = "Birmingham"
        weatherIcon.setImageResource(android.R.drawable.ic_menu_compass)
        weatherTemp.text = "19°C"
        weatherDesc.text = "Rain Shower"
        aqiValueText.text = aqiValue.toString()
        aqiLabel.text = "AQI"
        aqiDesc.text = "Unhealthy for Sensitive Groups"

        // AQI background color logic
        val aqiColor = when {
            aqiValue <= 50 -> Color.parseColor("#43A047") // Good - Green
            aqiValue <= 100 -> Color.parseColor("#FFEB3B") // Moderate - Yellow
            aqiValue <= 150 -> Color.parseColor("#FB8C00") // Unhealthy for Sensitive - Orange
            aqiValue <= 200 -> Color.parseColor("#D32F2F") // Unhealthy - Red
            aqiValue <= 300 -> Color.parseColor("#7B1FA2") // Very Unhealthy - Purple
            else -> Color.parseColor("#4E342E") // Hazardous - Brown
        }
        aqiCard.setCardBackgroundColor(aqiColor)

        // 2nd Section: Health Advice
        val healthAdviceCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.healthAdviceCard)
        val healthAdviceTitle = view.findViewById<TextView>(R.id.healthAdviceTitle)
        val healthAdviceText = view.findViewById<TextView>(R.id.healthAdviceText)
        healthAdviceTitle.setTextColor(aqiColor)
        healthAdviceCard.setCardBackgroundColor(aqiColor)
        healthAdviceText.text = "Members of sensitive groups may experience health effects. The general public is less likely to be affected."

        // 3rd Section: Forecast RecyclerView
        val forecastRecyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.forecastRecyclerView)
        val forecastList = listOf(
            ForecastItem("18:00", 5, "😊", android.R.drawable.ic_menu_compass, 20),
            ForecastItem("19:00", 2, "😊", android.R.drawable.ic_menu_compass, 21),
            ForecastItem("20:00", 3, "😊", android.R.drawable.ic_menu_compass, 22),
            ForecastItem("21:00", 2, "😊", android.R.drawable.ic_menu_compass, 21),
            ForecastItem("22:00", 1, "😊", android.R.drawable.ic_menu_compass, 20),
            ForecastItem("23:00", 2, "😊", android.R.drawable.ic_menu_compass, 20)
        )
        forecastRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        forecastRecyclerView.adapter = ForecastAdapter(forecastList)

        // 4th Section: Other Locations RecyclerView
        val otherLocationsRecyclerView = view.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.otherLocationsRecyclerView)
        val locationsList = listOf(
            LocationItem("Edmund Street", "Birmingham", 5, android.R.drawable.ic_menu_compass, 21),
            LocationItem("Berkley Street", "Birmingham", 4, android.R.drawable.ic_menu_compass, 21),
            LocationItem("Lalitpur", "Nepal", 7, android.R.drawable.ic_menu_compass, 19),
            LocationItem("Pokhara", "Nepal", 3, android.R.drawable.ic_menu_compass, 18)
        )
        otherLocationsRecyclerView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        otherLocationsRecyclerView.adapter = LocationAdapter(locationsList)

        // 6th Section: AQI Range Card click
        val aqiRangeCard = view.findViewById<com.google.android.material.card.MaterialCardView>(R.id.aqiRangeCard)
        aqiRangeCard.setOnClickListener {
            // TODO: Navigate to Air Quality Index Range page
        }
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
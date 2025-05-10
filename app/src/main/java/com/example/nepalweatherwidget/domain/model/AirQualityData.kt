package com.example.nepalweatherwidget.domain.model

import androidx.annotation.StringRes

data class AirQualityData(
    val aqi: Int,
    val pm25: Double,
    val pm10: Double,
    @StringRes val statusRes: Int,
    @StringRes val adviceRes: Int,
    @StringRes val healthRes: Int,
    val timestamp: Long = System.currentTimeMillis()
) 
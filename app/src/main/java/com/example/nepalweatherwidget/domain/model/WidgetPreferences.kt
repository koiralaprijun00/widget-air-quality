package com.example.nepalweatherwidget.domain.model

data class WidgetPreferences(
    val widgetId: Int,
    val locationName: String,
    val refreshInterval: String
) 
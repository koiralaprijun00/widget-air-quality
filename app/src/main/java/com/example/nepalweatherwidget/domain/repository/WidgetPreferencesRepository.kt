package com.example.nepalweatherwidget.domain.repository

import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WidgetPreferences

interface WidgetPreferencesRepository {
    suspend fun saveWidgetPreferences(
        widgetId: Int,
        locationName: String,
        refreshInterval: String
    ): Result<Unit>

    suspend fun getWidgetPreferences(widgetId: Int): Result<WidgetPreferences>
    
    suspend fun deleteWidgetPreferences(widgetId: Int): Result<Unit>
    
    suspend fun getAllWidgetPreferences(): Result<List<WidgetPreferences>>
} 
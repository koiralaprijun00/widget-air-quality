package com.example.nepalweatherwidget.features.widget.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.features.widget.domain.model.WidgetPreferences
import com.example.nepalweatherwidget.features.widget.domain.repository.WidgetPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class WidgetPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetPreferencesRepository {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    override fun getWidgetPreferences(widgetId: Int): Result<WidgetPreferences> {
        return try {
            val locationName = prefs.getString("widget_${widgetId}_location", "Kathmandu") ?: "Kathmandu"
            val updateInterval = prefs.getInt("widget_${widgetId}_interval", 30)
            Result.Success(WidgetPreferences(locationName, updateInterval))
        } catch (e: Exception) {
            Result.Error(e)
        }
    }

    override fun saveWidgetPreferences(widgetId: Int, preferences: WidgetPreferences) {
        prefs.edit().apply {
            putString("widget_${widgetId}_location", preferences.locationName)
            putInt("widget_${widgetId}_interval", preferences.updateIntervalMinutes)
            apply()
        }
    }

    companion object {
        private const val PREFS_NAME = "widget_preferences"
    }
} 
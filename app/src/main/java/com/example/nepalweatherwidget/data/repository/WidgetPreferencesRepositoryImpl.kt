package com.example.nepalweatherwidget.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.domain.model.WidgetPreferences
import com.example.nepalweatherwidget.domain.repository.WidgetPreferencesRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "widget_preferences")

@Singleton
class WidgetPreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WidgetPreferencesRepository {

    override suspend fun saveWidgetPreferences(
        widgetId: Int,
        locationName: String,
        refreshInterval: String
    ): Result<Unit> = try {
        context.dataStore.edit { preferences ->
            preferences[getLocationKey(widgetId)] = locationName
            preferences[getRefreshIntervalKey(widgetId)] = refreshInterval
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getWidgetPreferences(widgetId: Int): Result<WidgetPreferences> = try {
        val preferences = context.dataStore.data.first()
        val locationName = preferences[getLocationKey(widgetId)]
        val refreshInterval = preferences[getRefreshIntervalKey(widgetId)]

        if (locationName != null && refreshInterval != null) {
            Result.success(
                WidgetPreferences(
                    widgetId = widgetId,
                    locationName = locationName,
                    refreshInterval = refreshInterval
                )
            )
        } else {
            Result.failure(Exception("Widget preferences not found"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteWidgetPreferences(widgetId: Int): Result<Unit> = try {
        context.dataStore.edit { preferences ->
            preferences.remove(getLocationKey(widgetId))
            preferences.remove(getRefreshIntervalKey(widgetId))
        }
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAllWidgetPreferences(): Result<List<WidgetPreferences>> = try {
        val preferences = context.dataStore.data.first()
        val widgetIds = preferences.asMap().keys
            .filter { it.name.startsWith("location_") }
            .map { it.name.removePrefix("location_").toInt() }
            .distinct()

        val widgetPreferencesList = widgetIds.mapNotNull { widgetId ->
            val locationName = preferences[getLocationKey(widgetId)]
            val refreshInterval = preferences[getRefreshIntervalKey(widgetId)]

            if (locationName != null && refreshInterval != null) {
                WidgetPreferences(
                    widgetId = widgetId,
                    locationName = locationName,
                    refreshInterval = refreshInterval
                )
            } else null
        }

        Result.success(widgetPreferencesList)
    } catch (e: Exception) {
        Result.failure(e)
    }

    private fun getLocationKey(widgetId: Int) = stringPreferencesKey("location_$widgetId")
    private fun getRefreshIntervalKey(widgetId: Int) = stringPreferencesKey("refresh_interval_$widgetId")
} 
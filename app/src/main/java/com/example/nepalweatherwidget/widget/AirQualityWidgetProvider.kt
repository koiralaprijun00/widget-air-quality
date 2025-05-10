package com.example.nepalweatherwidget.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.data.repository.AirQualityRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AirQualityWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var repository: AirQualityRepository

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_air_quality)
        
        // Set initial loading state
        views.setTextViewText(R.id.tv_aqi_value, "--")
        views.setTextViewText(R.id.tv_location, "Loading...")
        appWidgetManager.updateAppWidget(appWidgetId, views)

        // Fetch and update data
        coroutineScope.launch {
            try {
                // TODO: Get actual location coordinates
                val data = repository.getAirQualityData(27.7172, 85.3240).first()
                
                views.setTextViewText(R.id.tv_aqi_value, data.aqi.toString())
                views.setTextViewText(R.id.tv_location, data.location)
                
                // Update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                views.setTextViewText(R.id.tv_aqi_value, "--")
                views.setTextViewText(R.id.tv_location, "Error")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }
} 
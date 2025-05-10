package com.example.nepalweatherwidget.features.widget.presentation

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.nepalweatherwidget.R
import com.example.nepalweatherwidget.features.widget.domain.repository.WidgetRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AirQualityWidgetProvider : AppWidgetProvider() {
    
    @Inject
    lateinit var widgetRepository: WidgetRepository
    
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
        
        // Update widget UI with air quality data
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
} 
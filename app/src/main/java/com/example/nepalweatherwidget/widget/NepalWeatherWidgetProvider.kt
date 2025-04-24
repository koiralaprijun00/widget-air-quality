package com.example.nepalweatherwidget.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker

class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d(TAG, "Providing glance content for widget")
        try {
            provideContent {
                SimpleWeatherContent()
            }
            Log.d(TAG, "Successfully provided glance content")
        } catch (e: Exception) {
            Log.e(TAG, "Error providing glance content", e)
        }
    }
    
    companion object {
        private const val TAG = "WeatherWidget"
    }
}

@Composable
private fun SimpleWeatherContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Kathmandu",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color(0xFF000000))
            )
        )
        
        Text(
            text = "25°C",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color(0xFF000000))
            )
        )
        
        Text(
            text = "Partly Cloudy",
            style = TextStyle(
                color = ColorProvider(Color(0xFF666666))
            )
        )
    }
}

class NepalWeatherWidgetProvider : GlanceAppWidgetReceiver() {
    private val TAG = "WidgetProvider"
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()
    
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received intent: ${intent.action}")
        when (intent.action) {
            AppWidgetManager.ACTION_APPWIDGET_UPDATE -> {
                Log.d(TAG, "Handling APPWIDGET_UPDATE")
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, NepalWeatherWidgetProvider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
            else -> super.onReceive(context, intent)
        }
    }
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        Log.d(TAG, "onUpdate called with ${appWidgetIds.size} widgets")
        try {
            // Call super first
            super.onUpdate(context, appWidgetManager, appWidgetIds)
            
            // Update each widget in a coroutine
            CoroutineScope(Dispatchers.Default).launch {
                appWidgetIds.forEach { appWidgetId ->
                    try {
                        // Trigger a widget update through the system
                        val updateIntent = Intent(context, NepalWeatherWidgetProvider::class.java)
                        updateIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, intArrayOf(appWidgetId))
                        context.sendBroadcast(updateIntent)
                        Log.d(TAG, "Updated widget with ID: $appWidgetId")
                    } catch (e: Exception) {
                        Log.e(TAG, "Error updating widget with ID: $appWidgetId", e)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in onUpdate", e)
        }
    }
    
    override fun onEnabled(context: Context) {
        Log.d(TAG, "Widget enabled for the first time")
        try {
            super.onEnabled(context)
            // Start the update worker
            WeatherUpdateWorker.startPeriodicUpdate(context)
            Log.d(TAG, "Successfully enabled widget and started worker")
        } catch (e: Exception) {
            Log.e(TAG, "Error enabling widget", e)
        }
    }
    
    override fun onDisabled(context: Context) {
        Log.d(TAG, "All widget instances removed")
        try {
            super.onDisabled(context)
            Log.d(TAG, "Successfully disabled widget")
        } catch (e: Exception) {
            Log.e(TAG, "Error disabling widget", e)
        }
    }
}
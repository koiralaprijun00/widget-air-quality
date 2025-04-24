package com.example.nepalweatherwidget.widget

import android.content.Context
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
import androidx.glance.appwidget.cornerRadius
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider

class WeatherWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WeatherWidgetContent()
        }
    }
}

@Composable
private fun WeatherWidgetContent() {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Color.White)
            .cornerRadius(16.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Kathmandu, Nepal",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = ColorProvider(Color.Black)
            )
        )
        
        Text(
            text = "25°C",
            style = TextStyle(
                fontWeight = FontWeight.Bold
            )
        )
        
        Text(
            text = "Partly Cloudy"
        )
    }
}

class NepalWeatherWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()
}
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
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.appwidget.cornerRadius
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle

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
            .padding(16.dp)
            .background(
                ColorProvider(
                    Color(0xFFF5F5F5), // Light gray background
                    Color(0xFF1A1A1A)  // Dark background for dark mode
                )
            )
            .cornerRadius(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        // Location
        Text(
            text = "Kathmandu, Nepal",
            style = TextStyle(
                fontWeight = FontWeight.Medium,
                color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
            )
        )

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Temperature and Weather
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "25°C",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Text(
                text = "Partly Cloudy",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        // Air Quality
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AQI: 45",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Text(
                text = "PM2.5: 12",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )
        }
    }
}

class NepalWeatherWidgetProvider : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeatherWidget()
}
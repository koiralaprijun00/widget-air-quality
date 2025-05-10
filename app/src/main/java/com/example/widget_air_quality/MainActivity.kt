package com.example.widget_air_quality

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.widget_air_quality.ui.screens.AirQualityScreen
import com.example.widget_air_quality.ui.theme.WidgetAirQualityTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WidgetAirQualityTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // For testing, we'll use Kathmandu's coordinates
                    AirQualityScreen(
                        latitude = 27.7172,
                        longitude = 85.3240
                    )
                }
            }
        }
    }
} 
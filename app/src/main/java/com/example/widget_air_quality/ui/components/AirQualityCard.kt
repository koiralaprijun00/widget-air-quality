package com.example.widget_air_quality.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.widget_air_quality.data.model.AirPollutionData
import com.example.widget_air_quality.data.model.AirQualityIndex
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AirQualityCard(
    airPollutionData: AirPollutionData,
    modifier: Modifier = Modifier
) {
    val aqi = AirQualityIndex.fromValue(airPollutionData.main.aqi)
    val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val time = dateFormat.format(Date(airPollutionData.dt * 1000))

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when (aqi) {
                AirQualityIndex.GOOD -> Color(0xFF4CAF50)
                AirQualityIndex.FAIR -> Color(0xFF8BC34A)
                AirQualityIndex.MODERATE -> Color(0xFFFFEB3B)
                AirQualityIndex.POOR -> Color(0xFFFF9800)
                AirQualityIndex.VERY_POOR -> Color(0xFFF44336)
            }
        )
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Air Quality Index",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = aqi.description,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PollutionMetric("PM2.5", airPollutionData.components.pm2_5)
                PollutionMetric("PM10", airPollutionData.components.pm10)
                PollutionMetric("O3", airPollutionData.components.o3)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PollutionMetric("NO2", airPollutionData.components.no2)
                PollutionMetric("SO2", airPollutionData.components.so2)
                PollutionMetric("CO", airPollutionData.components.co)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Last updated: $time",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PollutionMetric(
    label: String,
    value: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = String.format("%.1f", value),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.White
        )
    }
} 
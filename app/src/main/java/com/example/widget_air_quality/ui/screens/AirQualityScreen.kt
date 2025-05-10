package com.example.widget_air_quality.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.widget_air_quality.ui.components.AirQualityCard
import com.example.widget_air_quality.ui.viewmodel.AirPollutionViewModel

@Composable
fun AirQualityScreen(
    viewModel: AirPollutionViewModel = hiltViewModel(),
    latitude: Double,
    longitude: Double
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(latitude, longitude) {
        viewModel.fetchCurrentAirPollution(latitude, longitude)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        when {
            uiState.isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            uiState.error != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Error: ${uiState.error}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { viewModel.fetchCurrentAirPollution(latitude, longitude) }
                    ) {
                        Text("Retry")
                    }
                }
            }
            uiState.currentAirPollution != null -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    uiState.currentAirPollution.list.firstOrNull()?.let { currentData ->
                        AirQualityCard(
                            airPollutionData = currentData,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
} 
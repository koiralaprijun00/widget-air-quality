package com.example.nepalweatherwidget.widget

import android.content.Context
import android.location.Location
import android.util.Log // Added for logging
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
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.location.LocationService
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import kotlin.Result

sealed class WidgetState<out T> {
    object Loading : WidgetState<Nothing>()
    data class Success<T>(val data: T) : WidgetState<T>()
    data class Error(val message: String) : WidgetState<Nothing>()
}

data class WeatherWidgetState(
    val location: Location? = null,
    val weatherData: WidgetState<Pair<WeatherData, AirQuality>> = WidgetState.Loading
)

// --- WeatherWidget is now a top-level class ---
// It needs the dependencies, so we add them to the constructor
class WeatherWidget(
    private val weatherRepository: WeatherRepository,
    private val locationService: LocationService
) : GlanceAppWidget() { // No longer inner, no longer needs @AndroidEntryPoint directly

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Fetch state using dependencies passed via constructor
        Log.d("WeatherWidget", "Providing glance content...")
        val state = try {
            val location = withContext(Dispatchers.IO) {
                Log.d("WeatherWidget", "Fetching last location...")
                locationService.getLastLocation() // Use injected service
            }
            Log.d("WeatherWidget", "Location result: $location")

            if (location != null) {
                Log.d("WeatherWidget", "Fetching weather and air quality...")
                val weatherResult = weatherRepository.getWeatherAndAirQuality(
                    location.latitude,
                    location.longitude
                )
                Log.d("WeatherWidget", "Weather result: $weatherResult")
                
                WeatherWidgetState(
                    location = location,
                    weatherData = weatherResult.fold(
                        onSuccess = { WidgetState.Success(it) },
                        onFailure = { WidgetState.Error(it.message ?: "Unknown error") }
                    )
                )
            } else {
                Log.w("WeatherWidget", "Location unavailable.")
                WeatherWidgetState(
                    weatherData = WidgetState.Error("Location unavailable")
                )
            }
        } catch (e: Exception) {
            Log.e("WeatherWidget", "Failed to fetch weather data", e)
            WeatherWidgetState(
                weatherData = WidgetState.Error("Failed to fetch weather data: ${e.message}")
            )
        }

        provideContent {
            WeatherWidgetContent(
                location = state.location,
                weatherState = state.weatherData
            )
        }
    }
}

// --- The Provider remains the Hilt Entry Point ---
@AndroidEntryPoint
class NepalWeatherWidgetProvider : GlanceAppWidgetReceiver() {
    // Dependencies are injected here
    @Inject
    lateinit var weatherRepository: WeatherRepository

    @Inject
    lateinit var locationService: LocationService

    // Instantiate the top-level WeatherWidget using a lazy delegate
    // Pass the injected dependencies from the Provider to the Widget's constructor
    override val glanceAppWidget: GlanceAppWidget by lazy {
        Log.d("WidgetProvider", "Instantiating WeatherWidget lazily...")
        WeatherWidget(weatherRepository, locationService)
    }

    override fun onEnabled(context: Context) {
        super.onEnabled(context)
        Log.d("WidgetProvider", "Widget enabled, starting periodic update worker.")
        WeatherUpdateWorker.startPeriodicUpdate(context)
    }
}

// --- Composable functions remain the same ---
// (Make sure these are still present below in your file)
@Composable
private fun WeatherWidgetContent(
    location: Location?,
    weatherState: WidgetState<Pair<WeatherData, AirQuality>>
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .defaultPadding()
            .background(
                ColorProvider(
                    Color(0xFFF5F5F5), // Light gray background
                    Color(0xFF1A1A1A)  // Dark background for dark mode
                )
            )
            .cornerRadius(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (location == null) {
            LocationUnavailableContent()
            return@Column
        }

        when (weatherState) {
            is WidgetState.Success -> {
                WeatherContent(weatherState.data.first, weatherState.data.second)
            }
            is WidgetState.Error -> {
                ErrorContent(weatherState.message)
            }
            is WidgetState.Loading -> {
                LoadingContent()
            }
        }
    }
}

@Composable
private fun WeatherContent(weather: WeatherData, airQuality: AirQuality) {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = weather.location,
                style = TextStyle(
                    fontWeight = FontWeight.Medium,
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${weather.temperature}°C",
                style = TextStyle(
                    fontWeight = FontWeight.Bold,
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Text(
                text = weather.description,
                style = TextStyle(
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )
        }

        Spacer(modifier = GlanceModifier.height(8.dp))

        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AQI: ${airQuality.aqi}",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )

            Spacer(modifier = GlanceModifier.width(8.dp))

            Text(
                text = "PM2.5: ${airQuality.pm25}",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
                )
            )
        }
    }
}

@Composable
private fun LocationUnavailableContent() {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Location unavailable",
            style = TextStyle(
                color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
            )
        )
        Text(
            text = "Please enable location services",
            style = TextStyle(
                color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
            )
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error",
            style = TextStyle(
                color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
            )
        )
        Text(
            text = message,
            style = TextStyle(
                color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
            )
        )
    }
}

@Composable
private fun LoadingContent() {
    Column(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Loading...",
            style = TextStyle(
                color = ColorProvider(Color(0xFF000000), Color(0xFFFFFFFF))
            )
        )
    }
}

@Composable
private fun GlanceModifier.appWidgetBackground() = this.background(
    ColorProvider(Color.White, Color.White)
)

fun GlanceModifier.defaultPadding() = this.padding(all = 12.dp)
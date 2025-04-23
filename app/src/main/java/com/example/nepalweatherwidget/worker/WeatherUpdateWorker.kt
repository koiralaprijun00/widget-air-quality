package com.example.nepalweatherwidget.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.nepalweatherwidget.data.repository.WeatherRepository
import com.example.nepalweatherwidget.data.util.Result as WeatherResult
import com.example.nepalweatherwidget.location.LocationService
import com.example.nepalweatherwidget.widget.WeatherWidget
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted private val appContext: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val locationService: LocationService
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d("WeatherUpdateWorker", "Starting background work...")
        try {
            val location = locationService.getLastLocation()
            if (location != null) {
                Log.d("WeatherUpdateWorker", "Location found: ${location.latitude}, ${location.longitude}")
                val repoResult = weatherRepository.getWeatherAndAirQuality(
                    location.latitude,
                    location.longitude
                )

                when (repoResult) {
                    is WeatherResult.Success -> {
                        Log.d("WeatherUpdateWorker", "Successfully fetched weather data.")
                        // Update all widget instances
                        val widget = WeatherWidget(weatherRepository, locationService)
                        val manager = GlanceAppWidgetManager(appContext)
                        val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
                        glanceIds.forEach { glanceId ->
                            widget.update(appContext, glanceId)
                        }
                        Result.success()
                    }
                    is WeatherResult.Error -> {
                        Log.w("WeatherUpdateWorker", "Error fetching weather data: ${repoResult.message}")
                        Result.retry()
                    }
                    is WeatherResult.Loading -> {
                        Log.d("WeatherUpdateWorker", "Weather data is loading, retrying later.")
                        Result.retry()
                    }
                }
            } else {
                Log.w("WeatherUpdateWorker", "Could not get location, retrying later.")
                Result.retry()
            }
        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Exception during work", e)
            Result.failure()
        }
    }

    companion object {
        private const val WORK_NAME = "WeatherUpdateWork"
        private const val UPDATE_INTERVAL_MINUTES = 30L

        fun startPeriodicUpdate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val updateRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                UPDATE_INTERVAL_MINUTES,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                updateRequest
            )
            Log.d("WeatherUpdateWorker", "Periodic work enqueued with UPDATE policy.")
        }
    }
}

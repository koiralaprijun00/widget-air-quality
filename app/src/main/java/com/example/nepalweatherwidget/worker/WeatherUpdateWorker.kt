package com.example.nepalweatherwidget.worker

import android.content.Context
import android.util.Log // Ensure Log is imported
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
// Correct import for periodic work policy
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ListenableWorker
// Removed unused imports (WorkInfo, Operation, ExistingWorkPolicy)
import com.example.nepalweatherwidget.data.repository.WeatherRepository
// Import your custom Result class
import com.example.nepalweatherwidget.data.util.Result
import com.example.nepalweatherwidget.location.LocationService
import com.example.nepalweatherwidget.widget.NepalWeatherWidgetProvider
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    // Removed redundant qualifier
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository,
    private val locationService: LocationService
) : CoroutineWorker(applicationContext, workerParams) { // Use applicationContext passed via @Assisted

    override suspend fun doWork(): ListenableWorker.Result = withContext(Dispatchers.IO) {
        Log.d("WeatherUpdateWorker", "Starting background work...")
        try {
            val location = locationService.getLastLocation()
            if (location != null) {
                Log.d("WeatherUpdateWorker", "Location found: ${location.latitude}, ${location.longitude}")
                // Assign to variable to allow smart casting
                val repoResult = weatherRepository.getWeatherAndAirQuality(
                    location.latitude,
                    location.longitude
                )

                // Use 'is' check for all branches including object for sealed class `when`
                when (repoResult) {
                    is Result.Success -> {
                        Log.d("WeatherUpdateWorker", "Successfully fetched weather data.")
                        // Update the widget with new data - repoResult.data holds the Pair
                        val provider = NepalWeatherWidgetProvider()
                        provider.updateWidget(applicationContext) // Pass context
                        ListenableWorker.Result.success()
                    }
                    is Result.Error -> {
                        // Access message via smart-casted repoResult
                        Log.w("WeatherUpdateWorker", "Error fetching weather data: ${repoResult.message}")
                        ListenableWorker.Result.retry()
                    }
                    // Correct check for Loading object
                    is Result.Loading -> {
                        Log.d("WeatherUpdateWorker", "Weather data is loading (API returned loading state), retrying later.")
                        ListenableWorker.Result.retry()
                    }
                    // Consider adding an else branch if Result could be extended later non-sealed
                    // else -> {
                    //     Log.e("WeatherUpdateWorker", "Unknown result state.")
                    //     ListenableWorker.Result.failure()
                    // }
                }
            } else {
                Log.w("WeatherUpdateWorker", "Could not get location, retrying later.")
                ListenableWorker.Result.retry()
            }
        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Exception during work", e)
            ListenableWorker.Result.failure()
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

            // Use UPDATE policy (replaces REPLACE)
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE, // Changed from REPLACE
                updateRequest
            )
            Log.d("WeatherUpdateWorker", "Periodic work enqueued with UPDATE policy.")
        }
    }
}
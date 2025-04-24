package com.example.nepalweatherwidget.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.example.nepalweatherwidget.widget.WeatherWidget
import com.example.nepalweatherwidget.domain.usecase.GetWeatherUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WeatherUpdateWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting background work...")
        try {
            // Get mock weather data
            val weatherData = GetWeatherUseCase.getMockWeatherData()
            Log.d(TAG, "Got weather data: $weatherData")

            // Update all widget instances
            val widget = WeatherWidget()
            val manager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
            
            Log.d(TAG, "Found ${glanceIds.size} widget instances to update")
            
            if (glanceIds.isEmpty()) {
                Log.d(TAG, "No widgets found to update")
                return@withContext Result.success()
            }
            
            glanceIds.forEach { glanceId ->
                try {
                    widget.update(applicationContext, glanceId)
                    Log.d(TAG, "Successfully updated widget with ID: $glanceId")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to update widget with ID: $glanceId", e)
                }
            }
            
            Log.d(TAG, "Successfully updated all widgets")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widgets", e)
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "WeatherUpdateWorker"
        private const val WORKER_TAG = "weather_update_worker"

        fun startPeriodicUpdate(context: Context) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val workRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
                15, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                    WORKER_TAG,
                    ExistingPeriodicWorkPolicy.UPDATE,
                    workRequest
                )
        }

        fun cancelPeriodicUpdate(context: Context) {
            WorkManager.getInstance(context)
                .cancelUniqueWork(WORKER_TAG)
        }
    }
}

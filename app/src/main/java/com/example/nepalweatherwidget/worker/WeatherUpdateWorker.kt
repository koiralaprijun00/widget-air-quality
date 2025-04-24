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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class WeatherUpdateWorker(
    private val appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): androidx.work.ListenableWorker.Result = withContext(Dispatchers.IO) {
        Log.d("WeatherUpdateWorker", "Starting background work...")
        try {
            // Update all widget instances
            val widget = WeatherWidget()
            val manager = GlanceAppWidgetManager(appContext)
            val glanceIds = manager.getGlanceIds(WeatherWidget::class.java)
            glanceIds.forEach { glanceId ->
                widget.update(appContext, glanceId)
            }
            Log.d("WeatherUpdateWorker", "Successfully updated widgets.")
            androidx.work.ListenableWorker.Result.success()
        } catch (e: Exception) {
            Log.e("WeatherUpdateWorker", "Failed to update widgets", e)
            androidx.work.ListenableWorker.Result.failure()
        }
    }

    companion object {
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
    }
}

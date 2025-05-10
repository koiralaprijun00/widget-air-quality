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
import com.example.nepalweatherwidget.domain.repository.WidgetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import dagger.assisted.AssistedFactory

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val widgetRepository: WidgetRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        Log.d(TAG, "Starting background work...")
        try {
            when (val widgetData = widgetRepository.getWidgetData()) {
                is com.example.nepalweatherwidget.domain.model.WidgetData.Success -> {
                    Log.d(TAG, "Successfully fetched widget data")
                    Result.success()
                }
                is com.example.nepalweatherwidget.domain.model.WidgetData.Error -> {
                    Log.e(TAG, "Error fetching widget data: ${widgetData.message}")
                    Result.retry()
                }
                is com.example.nepalweatherwidget.domain.model.WidgetData.Loading -> {
                    Log.d(TAG, "Widget data is loading")
                    Result.retry()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to update widget data", e)
            Result.retry()
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

    @AssistedFactory
    interface Factory {
        fun create(appContext: Context, params: WorkerParameters): WeatherUpdateWorker
    }
}

package com.example.nepalweatherwidget.features.weather.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.nepalweatherwidget.core.di.WorkerAssistedFactory
import com.example.nepalweatherwidget.features.weather.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@HiltWorker
class WeatherUpdateWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Get location from input data
            val location = inputData.getString(KEY_LOCATION) ?: return@withContext Result.failure()
            
            // Fetch weather data
            val result = weatherRepository.getWeatherData(location)
            
            when (result) {
                is com.example.nepalweatherwidget.core.result.Result.Success -> Result.success()
                is com.example.nepalweatherwidget.core.result.Result.Error -> Result.failure()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }

    @dagger.assisted.AssistedFactory
    interface Factory : WorkerAssistedFactory<WeatherUpdateWorker>

    companion object {
        const val KEY_LOCATION = "location"
    }
} 
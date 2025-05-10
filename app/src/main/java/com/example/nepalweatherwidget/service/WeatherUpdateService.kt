package com.example.nepalweatherwidget.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@AndroidEntryPoint
class WeatherUpdateService : Service() {

    @Inject
    lateinit var workManager: WorkManager

    override fun onCreate() {
        super.onCreate()
        scheduleWeatherUpdates()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun scheduleWeatherUpdates() {
        val weatherUpdateRequest = PeriodicWorkRequestBuilder<WeatherUpdateWorker>(
            15, TimeUnit.MINUTES
        ).build()

        workManager.enqueueUniquePeriodicWork(
            "weather_update_work",
            ExistingPeriodicWorkPolicy.KEEP,
            weatherUpdateRequest
        )
    }
} 
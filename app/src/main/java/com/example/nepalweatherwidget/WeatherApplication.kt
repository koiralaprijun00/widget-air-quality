package com.example.nepalweatherwidget

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import com.example.nepalweatherwidget.core.di.HiltWorkerFactory
import javax.inject.Inject

@HiltAndroidApp
class WeatherApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
} 
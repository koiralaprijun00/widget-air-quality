package com.example.nepalweatherwidget

import android.app.Application
import androidx.work.Configuration
import com.example.nepalweatherwidget.core.di.HiltWorkerFactory
import com.example.nepalweatherwidget.core.security.ApiKeyInitializer
import com.example.nepalweatherwidget.core.util.Logger
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class WeatherApplication : Application(), Configuration.Provider {
    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var apiKeyInitializer: ApiKeyInitializer

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onCreate() {
        super.onCreate()
        initializeApiKey()
    }

    private fun initializeApiKey() {
        applicationScope.launch {
            try {
                apiKeyInitializer.initializeApiKey()
            } catch (e: Exception) {
                Logger.e("WeatherApplication: Failed to initialize API key", e)
            }
        }
    }

    override fun getWorkManagerConfiguration() =
        Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
} 
package com.example.nepalweatherwidget.core.di.modules

import android.content.Context
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.nepalweatherwidget.core.di.WorkerAssistedFactory
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    @Provides
    @Singleton
    fun provideWorkManagerConfiguration(
        workerFactory: HiltWorkerFactory
    ): Configuration {
        return Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    }
    
    @Provides
    fun provideWorkerFactory(
        weatherUpdateWorkerFactory: WeatherUpdateWorker.Factory
    ): Map<Class<out ListenableWorker>, @JvmSuppressWildcards WeatherUpdateWorker.Factory> {
        return mapOf(
            WeatherUpdateWorker::class.java to weatherUpdateWorkerFactory
        )
    }
} 
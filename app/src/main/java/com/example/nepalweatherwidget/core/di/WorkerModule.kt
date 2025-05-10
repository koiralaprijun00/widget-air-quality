package com.example.nepalweatherwidget.core.di

import androidx.work.WorkerFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object WorkerModule {
    
    @Provides
    @Singleton
    fun provideWorkerFactory(
        workerFactories: Map<Class<out androidx.work.ListenableWorker>, @JvmSuppressWildcards WorkerAssistedFactory<out androidx.work.ListenableWorker>>
    ): WorkerFactory {
        return HiltWorkerFactory(workerFactories)
    }
} 
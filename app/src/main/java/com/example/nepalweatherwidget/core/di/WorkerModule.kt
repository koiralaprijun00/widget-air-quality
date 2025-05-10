package com.example.nepalweatherwidget.core.di

import android.content.Context
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.example.nepalweatherwidget.widget.WidgetUpdateWorker
import com.example.nepalweatherwidget.worker.WeatherUpdateWorker
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HiltWorkerFactory @Inject constructor(
    private val workerFactories: Map<Class<out ListenableWorker>, @JvmSuppressWildcards WorkerAssistedFactory<out ListenableWorker>>
) : WorkerFactory() {
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        val workerClass = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
        val factory = workerFactories[workerClass]
        return factory?.create(appContext, workerParameters)
    }
}

interface WorkerAssistedFactory<T : ListenableWorker> {
    fun create(appContext: Context, workerParameters: WorkerParameters): T
}

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
        weatherUpdateWorkerFactory: WeatherUpdateWorker.Factory,
        widgetUpdateWorkerFactory: WidgetUpdateWorker.Factory
    ): Map<Class<out ListenableWorker>, @JvmSuppressWildcards WorkerAssistedFactory<out ListenableWorker>> {
        return mapOf(
            WeatherUpdateWorker::class.java to weatherUpdateWorkerFactory,
            WidgetUpdateWorker::class.java to widgetUpdateWorkerFactory
        )
    }
} 
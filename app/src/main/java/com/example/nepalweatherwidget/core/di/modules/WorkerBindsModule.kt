package com.example.nepalweatherwidget.core.di

import androidx.work.ListenableWorker
import com.example.nepalweatherwidget.features.weather.worker.WeatherUpdateWorker
import com.example.nepalweatherwidget.features.widget.worker.WidgetUpdateWorker
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoMap

@Module
@InstallIn(SingletonComponent::class)
abstract class WorkerBindsModule {
    
    @Binds
    @IntoMap
    @WorkerKey(WeatherUpdateWorker::class)
    abstract fun bindWeatherUpdateWorkerFactory(
        factory: WeatherUpdateWorker.Factory
    ): WorkerAssistedFactory<out ListenableWorker>
    
    @Binds
    @IntoMap
    @WorkerKey(WidgetUpdateWorker::class)
    abstract fun bindWidgetUpdateWorkerFactory(
        factory: WidgetUpdateWorker.Factory
    ): WorkerAssistedFactory<out ListenableWorker>
} 
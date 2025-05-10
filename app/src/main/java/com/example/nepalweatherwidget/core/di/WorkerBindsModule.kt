package com.example.nepalweatherwidget.core.di

import com.example.nepalweatherwidget.features.weather.worker.WeatherUpdateWorker
import com.example.nepalweatherwidget.features.widget.worker.WidgetUpdateWorker
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
interface WorkerBindsModule {
    
    @Binds
    @IntoMap
    @WorkerKey(WeatherUpdateWorker::class)
    fun bindWeatherUpdateWorkerFactory(
        factory: WeatherUpdateWorker.Factory
    ): WorkerAssistedFactory<WeatherUpdateWorker>
    
    @Binds
    @IntoMap
    @WorkerKey(WidgetUpdateWorker::class)
    fun bindWidgetUpdateWorkerFactory(
        factory: WidgetUpdateWorker.Factory
    ): WorkerAssistedFactory<WidgetUpdateWorker>
} 
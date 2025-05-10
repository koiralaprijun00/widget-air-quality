package com.example.nepalweatherwidget.core.di

import android.content.Context
import com.example.nepalweatherwidget.features.weather.data.repository.WeatherRepositoryImpl
import com.example.nepalweatherwidget.features.weather.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.features.widget.data.repository.WidgetPreferencesRepositoryImpl
import com.example.nepalweatherwidget.features.widget.domain.repository.WidgetPreferencesRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository {
        return weatherRepositoryImpl
    }

    @Provides
    @Singleton
    fun provideWidgetPreferencesRepository(
        @ApplicationContext context: Context
    ): WidgetPreferencesRepository {
        return WidgetPreferencesRepositoryImpl(context)
    }
} 
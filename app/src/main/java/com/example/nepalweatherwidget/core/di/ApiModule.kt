package com.example.nepalweatherwidget.core.di

import com.example.nepalweatherwidget.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    fun provideOpenWeatherApiKey(): String {
        return BuildConfig.OPENWEATHER_API_KEY
    }
} 
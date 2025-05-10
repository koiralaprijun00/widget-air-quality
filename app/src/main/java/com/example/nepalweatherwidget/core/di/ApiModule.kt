package com.example.nepalweatherwidget.core.di

import com.example.nepalweatherwidget.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {
    @Provides
    @Singleton
    @Named("openweather_api_key")
    fun provideOpenWeatherApiKey(): String {
        return BuildConfig.OPENWEATHER_API_KEY
    }
} 
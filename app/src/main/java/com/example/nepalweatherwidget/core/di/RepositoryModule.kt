package com.example.nepalweatherwidget.core.di

import com.example.nepalweatherwidget.data.repository.WeatherRepositoryImpl
import com.example.nepalweatherwidget.data.repository.GeocodingRepositoryImpl
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.GeocodingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindWeatherRepository(
        weatherRepositoryImpl: WeatherRepositoryImpl
    ): WeatherRepository

    @Binds
    @Singleton
    abstract fun bindGeocodingRepository(
        geocodingRepositoryImpl: GeocodingRepositoryImpl
    ): GeocodingRepository
} 
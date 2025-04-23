package com.example.nepalweatherwidget.di

import com.example.nepalweatherwidget.data.repository.WeatherRepository
import com.example.nepalweatherwidget.domain.repository.WeatherRepository as DomainWeatherRepository
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
        weatherRepository: WeatherRepository
    ): DomainWeatherRepository
} 
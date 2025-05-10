package com.example.nepalweatherwidget.di

import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.remote.api.WeatherService
import com.example.nepalweatherwidget.data.repository.WeatherRepositoryImpl
import com.example.nepalweatherwidget.domain.network.NetworkMonitor
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherService: WeatherService,
        airPollutionService: AirPollutionService,
        networkMonitor: NetworkMonitor
    ): WeatherRepository {
        return WeatherRepositoryImpl(weatherService, airPollutionService, networkMonitor)
    }
} 
package com.example.nepalweatherwidget.di

import com.example.nepalweatherwidget.domain.network.NetworkMonitor
import com.example.nepalweatherwidget.domain.network.NetworkMonitorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkMonitorModule {
    @Binds
    @Singleton
    abstract fun bindNetworkMonitor(
        networkMonitorImpl: NetworkMonitorImpl
    ): NetworkMonitor
} 
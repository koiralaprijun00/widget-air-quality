package com.example.nepalweatherwidget.core.di

import com.example.nepalweatherwidget.features.dashboard.presentation.DashboardViewModel
import com.example.nepalweatherwidget.features.weather.presentation.WeatherViewModel
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.scopes.ViewModelScoped

@Module
@InstallIn(ViewModelComponent::class)
object ViewModelModule {
    // ViewModels are provided by Hilt automatically
} 
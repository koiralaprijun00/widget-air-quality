package com.example.nepalweatherwidget.core.di

import com.example.nepalweatherwidget.core.di.modules.DatabaseModule
import com.example.nepalweatherwidget.core.di.modules.NetworkModule
import com.example.nepalweatherwidget.core.di.modules.RepositoryModule
import com.example.nepalweatherwidget.core.di.modules.SecurityModule
import com.example.nepalweatherwidget.core.di.modules.ViewModelModule
import com.example.nepalweatherwidget.core.di.modules.WorkerBindsModule
import com.example.nepalweatherwidget.core.di.modules.WorkerModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(
    includes = [
        NetworkModule::class,
        DatabaseModule::class,
        SecurityModule::class,
        RepositoryModule::class,
        WorkerModule::class,
        WorkerBindsModule::class,
        ViewModelModule::class
    ]
)
@InstallIn(SingletonComponent::class)
object AppModule 
package com.example.nepalweatherwidget.core.di

import android.content.Context
import androidx.room.Room
import com.example.nepalweatherwidget.features.weather.data.local.WeatherDatabase
import com.example.nepalweatherwidget.features.weather.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.features.weather.data.local.dao.WeatherDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideWeatherDatabase(
        @ApplicationContext context: Context
    ): WeatherDatabase {
        return Room.databaseBuilder(
            context,
            WeatherDatabase::class.java,
            "weather_database"
        ).build()
    }

    @Provides
    fun provideWeatherDao(database: WeatherDatabase): WeatherDao {
        return database.weatherDao()
    }

    @Provides
    fun provideAirQualityDao(database: WeatherDatabase): AirQualityDao {
        return database.airQualityDao()
    }
} 
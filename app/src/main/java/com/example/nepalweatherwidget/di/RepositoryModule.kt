package com.example.nepalweatherwidget.di

import android.content.Context
import androidx.room.Room
import com.example.nepalweatherwidget.data.local.WeatherDatabase
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.remote.api.WeatherService
import com.example.nepalweatherwidget.data.repository.WeatherRepositoryImpl
import com.example.nepalweatherwidget.domain.network.NetworkMonitor
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
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
    @Singleton
    fun provideWeatherDao(database: WeatherDatabase): WeatherDao {
        return database.weatherDao()
    }

    @Provides
    @Singleton
    fun provideAirQualityDao(database: WeatherDatabase): AirQualityDao {
        return database.airQualityDao()
    }

    @Provides
    @Singleton
    fun provideWeatherRepository(
        weatherService: WeatherService,
        airPollutionService: AirPollutionService,
        networkMonitor: NetworkMonitor,
        weatherDao: WeatherDao,
        airQualityDao: AirQualityDao,
        apiKey: String
    ): WeatherRepository {
        return WeatherRepositoryImpl(
            weatherService = weatherService,
            airPollutionService = airPollutionService,
            networkMonitor = networkMonitor,
            weatherDao = weatherDao,
            airQualityDao = airQualityDao,
            apiKey = apiKey
        )
    }
} 
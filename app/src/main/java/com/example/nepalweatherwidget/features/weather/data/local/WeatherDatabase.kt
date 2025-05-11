package com.example.nepalweatherwidget.features.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nepalweatherwidget.features.weather.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.features.weather.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.features.weather.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.features.weather.data.local.entity.AirQualityEntity

@Database(
    entities = [WeatherEntity::class, AirQualityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun airQualityDao(): AirQualityDao
} 
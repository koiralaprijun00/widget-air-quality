package com.example.nepalweatherwidget.features.weather.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nepalweatherwidget.features.weather.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.features.weather.data.local.entity.WeatherEntity

@Database(
    entities = [WeatherEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
} 
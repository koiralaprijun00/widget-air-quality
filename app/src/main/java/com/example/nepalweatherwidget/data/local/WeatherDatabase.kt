package com.example.nepalweatherwidget.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.local.entity.AirQualityEntity
import com.example.nepalweatherwidget.data.local.entity.WeatherEntity

@Database(
    entities = [WeatherEntity::class, AirQualityEntity::class],
    version = 1,
    exportSchema = false
)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun airQualityDao(): AirQualityDao
} 
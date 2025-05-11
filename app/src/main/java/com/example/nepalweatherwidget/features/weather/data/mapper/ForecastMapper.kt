package com.example.nepalweatherwidget.features.weather.data.mapper

import com.example.nepalweatherwidget.data.remote.model.ForecastResponse
import com.example.nepalweatherwidget.features.weather.domain.model.ForecastItem
import com.example.nepalweatherwidget.features.weather.domain.model.WeatherData
import com.example.nepalweatherwidget.features.weather.util.WeatherIconUtil.getWeatherIcon
import com.example.nepalweatherwidget.features.weather.util.AirQualityUtil.getAqiEmoji
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ForecastMapper @Inject constructor(
    private val baseMapper: BaseWeatherMapper
) {
    fun toDomain(response: ForecastResponse): List<WeatherData> {
        return baseMapper.toWeatherDataList(response)
    }
    
    fun toForecastItem(weatherData: WeatherData): ForecastItem {
        val hour = SimpleDateFormat("HH:mm", Locale.getDefault())
            .format(Date(weatherData.timestamp))
        
        return ForecastItem(
            hour = hour,
            temperature = weatherData.temperature,
            aqi = 0, // TODO: Get from air quality data
            aqiEmoji = getAqiEmoji(0),
            weatherIconRes = getWeatherIcon(weatherData.iconCode)
        )
    }
} 
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
class ForecastMapper @Inject constructor() {
    
    fun toDomain(response: ForecastResponse): List<WeatherData> {
        return response.list.map { forecastItem ->
            WeatherData(
                temperature = forecastItem.main.temp,
                feelsLike = forecastItem.main.feelsLike,
                description = forecastItem.weather.firstOrNull()?.description ?: "",
                iconCode = forecastItem.weather.firstOrNull()?.icon ?: "",
                humidity = forecastItem.main.humidity,
                windSpeed = forecastItem.wind.speed,
                timestamp = forecastItem.dt * 1000
            )
        }
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
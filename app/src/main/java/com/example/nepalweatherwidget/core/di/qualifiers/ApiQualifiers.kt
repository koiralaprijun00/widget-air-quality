package com.example.nepalweatherwidget.core.di.qualifiers

import javax.inject.Qualifier

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenWeatherApiKey

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class WeatherApiService

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class AirQualityApiService 
package com.example.nepalweatherwidget.core.di

import com.example.nepalweatherwidget.core.security.ApiKeyManager
import com.example.nepalweatherwidget.features.weather.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.features.weather.data.remote.api.GeocodingService
import com.example.nepalweatherwidget.features.weather.data.remote.api.WeatherService
import com.example.nepalweatherwidget.core.di.qualifiers.WeatherApiService
import com.example.nepalweatherwidget.core.di.qualifiers.AirQualityApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.openweathermap.org", 
                 "sha256/axmGTWYycVN5oCjh3GJrxWVndLSZjypDO6evrHMwbXg=") // Real hash for OpenWeather API
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        apiKeyManager: ApiKeyManager,
        certificatePinner: CertificatePinner
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val apiKey = apiKeyManager.getApiKey()
                val url = chain.request().url.newBuilder()
                    .addQueryParameter("appid", apiKey)
                    .build()
                
                chain.proceed(chain.request().newBuilder().url(url).build())
            }
            .certificatePinner(certificatePinner)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    @WeatherApiService
    fun provideWeatherService(retrofit: Retrofit): WeatherService {
        return retrofit.create(WeatherService::class.java)
    }

    @Provides
    @Singleton
    @AirQualityApiService
    fun provideAirPollutionService(retrofit: Retrofit): AirPollutionService {
        return retrofit.create(AirPollutionService::class.java)
    }

    @Provides
    @Singleton
    fun provideGeocodingService(retrofit: Retrofit): GeocodingService {
        return retrofit.create(GeocodingService::class.java)
    }
} 
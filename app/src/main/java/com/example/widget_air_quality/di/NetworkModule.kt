package com.example.widget_air_quality.di

import com.example.widget_air_quality.data.api.AirPollutionService
import com.example.widget_air_quality.data.repository.AirPollutionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "http://api.openweathermap.org/"
    private const val API_KEY = "a084f03dedc3e73373fdbcd8e6452d22"

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAirPollutionService(retrofit: Retrofit): AirPollutionService {
        return retrofit.create(AirPollutionService::class.java)
    }

    @Provides
    @Singleton
    fun provideAirPollutionRepository(service: AirPollutionService): AirPollutionRepository {
        return AirPollutionRepository(service, API_KEY)
    }
} 
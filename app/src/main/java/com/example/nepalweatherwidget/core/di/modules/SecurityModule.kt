package com.example.nepalweatherwidget.core.di.modules

import android.content.Context
import com.example.nepalweatherwidget.core.di.qualifiers.OpenWeatherApiKey
import com.example.nepalweatherwidget.core.security.ApiKeyManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.CertificatePinner
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SecurityModule {
    
    @Provides
    @Singleton
    fun provideApiKeyManager(
        @ApplicationContext context: Context
    ): ApiKeyManager {
        return ApiKeyManager(context)
    }
    
    @Provides
    @Singleton
    @OpenWeatherApiKey
    fun provideOpenWeatherApiKey(apiKeyManager: ApiKeyManager): String {
        return apiKeyManager.getApiKey()
    }
    
    @Provides
    @Singleton
    fun provideCertificatePinner(): CertificatePinner {
        return CertificatePinner.Builder()
            .add("api.openweathermap.org", 
                 "sha256/axmGTWYycVN5oCjh3GJrxWVndLSZjypDO6evrHMwbXg=")
            .build()
    }
} 
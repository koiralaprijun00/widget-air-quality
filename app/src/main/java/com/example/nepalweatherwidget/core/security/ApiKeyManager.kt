package com.example.nepalweatherwidget.core.security

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nepalweatherwidget.BuildConfig
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()
        
    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        "secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
    
    fun getApiKey(): String {
        // For development: Use BuildConfig
        if (BuildConfig.DEBUG) {
            return BuildConfig.OPENWEATHER_API_KEY
        }
        
        // For production: Get from encrypted storage
        return encryptedPrefs.getString("api_key", "") ?: ""
    }
    
    suspend fun saveApiKey(apiKey: String) {
        encryptedPrefs.edit {
            putString("api_key", apiKey)
        }
    }
} 
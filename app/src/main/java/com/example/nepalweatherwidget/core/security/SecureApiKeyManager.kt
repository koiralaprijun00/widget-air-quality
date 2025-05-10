package com.example.nepalweatherwidget.core.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nepalweatherwidget.BuildConfig
import com.example.nepalweatherwidget.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val sharedPreferences: SharedPreferences
) {
    
    companion object {
        private const val ENCRYPTED_API_KEY_PREF = "encrypted_api_key"
        private const val API_KEY_ALIAS = "api_key_alias"
        private const val API_KEY_VALIDATION_TIME = "api_key_validation_time"
        private const val VALIDATION_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
    }
    
    private val encryptedSharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    suspend fun getApiKey(): String {
        return try {
            // Check if API key needs validation
            if (shouldValidateApiKey()) {
                validateApiKey()
            }
            
            // Get from encrypted storage
            encryptedSharedPreferences.getString(ENCRYPTED_API_KEY_PREF, null)
                ?: fetchAndStoreApiKey()
        } catch (e: Exception) {
            Logger.e("SecureApiKeyManager: Failed to get API key", e)
            throw SecurityException("Failed to retrieve API key", e)
        }
    }
    
    private suspend fun fetchAndStoreApiKey(): String {
        // In production, this would fetch from a secure backend
        // For now, use the BuildConfig as fallback
        val apiKey = BuildConfig.OPENWEATHER_API_KEY
        
        if (apiKey.isBlank()) {
            throw IllegalStateException("API key is not configured")
        }
        
        // Store encrypted
        encryptedSharedPreferences.edit()
            .putString(ENCRYPTED_API_KEY_PREF, apiKey)
            .apply()
        
        return apiKey
    }
    
    private fun shouldValidateApiKey(): Boolean {
        val lastValidation = sharedPreferences.getLong(API_KEY_VALIDATION_TIME, 0)
        return System.currentTimeMillis() - lastValidation > VALIDATION_INTERVAL_MS
    }
    
    private suspend fun validateApiKey() {
        // Implement API key validation logic
        // This could involve a simple API call to check if the key is still valid
        try {
            val apiKey = encryptedSharedPreferences.getString(ENCRYPTED_API_KEY_PREF, null)
            if (apiKey != null) {
                // Make a lightweight API call to validate the key
                // Update validation timestamp on success
                sharedPreferences.edit()
                    .putLong(API_KEY_VALIDATION_TIME, System.currentTimeMillis())
                    .apply()
            }
        } catch (e: Exception) {
            Logger.e("SecureApiKeyManager: API key validation failed", e)
            // Consider invalidating the key and fetching a new one
            clearApiKey()
            fetchAndStoreApiKey()
        }
    }
    
    fun clearApiKey() {
        encryptedSharedPreferences.edit()
            .remove(ENCRYPTED_API_KEY_PREF)
            .apply()
    }
} 
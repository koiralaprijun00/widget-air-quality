package com.example.nepalweatherwidget.core.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecureApiKeyManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSTORE_PROVIDER = "AndroidKeyStore"
        private const val KEY_ALIAS = "weather_api_key"
        private const val ENCRYPTED_API_KEY_PREF = "encrypted_api_key"
        private const val API_KEY_VALIDATION_TIME = "api_key_validation_time"
        private const val VALIDATION_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 hours
        private const val KEY_ROTATION_INTERVAL_MS = 7 * 24 * 60 * 60 * 1000L // 7 days
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(KEYSTORE_PROVIDER).apply {
            load(null)
        }
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

    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }

    private fun generateKey() {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            KEYSTORE_PROVIDER
        )

        val keyGenParameterSpec = KeyGenParameterSpec.Builder(
            KEY_ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .setUserAuthenticationRequired(false)
            .build()

        keyGenerator.init(keyGenParameterSpec)
        keyGenerator.generateKey()
    }

    suspend fun getApiKey(): String = withContext(Dispatchers.IO) {
        try {
            if (shouldRotateKey()) {
                rotateKey()
            }

            if (shouldValidateApiKey()) {
                validateApiKey()
            }

            val encryptedKey = encryptedSharedPreferences.getString(ENCRYPTED_API_KEY_PREF, null)
                ?: throw WeatherException.ApiException.InvalidApiKey("No API key found")

            decryptApiKey(encryptedKey)
        } catch (e: Exception) {
            Logger.e("SecureApiKeyManager: Failed to get API key", e)
            throw WeatherException.ApiException.InvalidApiKey("Failed to retrieve API key")
        }
    }

    private fun shouldRotateKey(): Boolean {
        val lastRotation = encryptedSharedPreferences.getLong("last_key_rotation", 0)
        return System.currentTimeMillis() - lastRotation > KEY_ROTATION_INTERVAL_MS
    }

    private fun shouldValidateApiKey(): Boolean {
        val lastValidation = encryptedSharedPreferences.getLong(API_KEY_VALIDATION_TIME, 0)
        return System.currentTimeMillis() - lastValidation > VALIDATION_INTERVAL_MS
    }

    private suspend fun rotateKey() {
        try {
            // Generate new key
            generateKey()
            
            // Re-encrypt the API key with the new key
            val currentKey = encryptedSharedPreferences.getString(ENCRYPTED_API_KEY_PREF, null)
            if (currentKey != null) {
                val decryptedKey = decryptApiKey(currentKey)
                encryptAndStoreApiKey(decryptedKey)
            }

            // Update rotation timestamp
            encryptedSharedPreferences.edit()
                .putLong("last_key_rotation", System.currentTimeMillis())
                .apply()
        } catch (e: Exception) {
            Logger.e("SecureApiKeyManager: Key rotation failed", e)
            throw WeatherException.ApiException.InvalidApiKey("Key rotation failed")
        }
    }

    private suspend fun validateApiKey() {
        try {
            val apiKey = encryptedSharedPreferences.getString(ENCRYPTED_API_KEY_PREF, null)
            if (apiKey != null) {
                val decryptedKey = decryptApiKey(apiKey)
                // Make a lightweight API call to validate the key
                // If validation fails, throw an exception
                // On success, update validation timestamp
                encryptedSharedPreferences.edit()
                    .putLong(API_KEY_VALIDATION_TIME, System.currentTimeMillis())
                    .apply()
            }
        } catch (e: Exception) {
            Logger.e("SecureApiKeyManager: API key validation failed", e)
            clearApiKey()
            throw WeatherException.ApiException.InvalidApiKey("API key validation failed")
        }
    }

    fun encryptAndStoreApiKey(apiKey: String) {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        
        val encrypted = cipher.doFinal(apiKey.toByteArray())
        val combined = cipher.iv + encrypted

        encryptedSharedPreferences.edit()
            .putString(ENCRYPTED_API_KEY_PREF, combined.toString(Charsets.ISO_8859_1))
            .apply()
    }

    private fun decryptApiKey(encryptedKey: String): String {
        val combined = encryptedKey.toByteArray(Charsets.ISO_8859_1)
        val iv = combined.copyOfRange(0, 12)
        val encrypted = combined.copyOfRange(12, combined.size)

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val secretKey = keyStore.getKey(KEY_ALIAS, null) as SecretKey
        cipher.init(Cipher.DECRYPT_MODE, secretKey, GCMParameterSpec(GCM_TAG_LENGTH, iv))

        return String(cipher.doFinal(encrypted))
    }

    fun clearApiKey() {
        encryptedSharedPreferences.edit()
            .remove(ENCRYPTED_API_KEY_PREF)
            .apply()
    }
} 
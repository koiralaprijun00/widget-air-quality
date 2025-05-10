package com.example.nepalweatherwidget.core.security

import android.content.Context
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.util.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApiKeyInitializer @Inject constructor(
    @ApplicationContext private val context: Context,
    private val secureApiKeyManager: SecureApiKeyManager,
    private val okHttpClient: OkHttpClient
) {
    companion object {
        private const val API_KEY_ENDPOINT = "https://your-secure-backend.com/api/keys" // Replace with your actual endpoint
        private const val MAX_RETRIES = 3
        private const val RETRY_DELAY_MS = 1000L
    }

    suspend fun initializeApiKey(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Check if we already have a valid key
            if (isApiKeyValid()) {
                return@withContext Result.success(Unit)
            }

            // Fetch new key from secure backend
            val apiKey = fetchApiKeyFromServer()
            secureApiKeyManager.encryptAndStoreApiKey(apiKey)
            
            Result.success(Unit)
        } catch (e: Exception) {
            Logger.e("ApiKeyInitializer: Failed to initialize API key", e)
            Result.failure(e)
        }
    }

    private suspend fun isApiKeyValid(): Boolean {
        return try {
            secureApiKeyManager.getApiKey()
            true
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun fetchApiKeyFromServer(): String {
        var retryCount = 0
        var lastException: Exception? = null

        while (retryCount < MAX_RETRIES) {
            try {
                val request = Request.Builder()
                    .url(API_KEY_ENDPOINT)
                    .build()

                val response = okHttpClient.newCall(request).execute()
                if (!response.isSuccessful) {
                    throw WeatherException.ApiException.HttpError(
                        code = response.code,
                        message = "Failed to fetch API key: ${response.message}"
                    )
                }

                val apiKey = response.body?.string()
                    ?: throw WeatherException.ApiException.InvalidApiKey("Empty API key response")

                if (apiKey.isBlank()) {
                    throw WeatherException.ApiException.InvalidApiKey("Blank API key received")
                }

                return apiKey
            } catch (e: Exception) {
                lastException = e
                retryCount++
                if (retryCount < MAX_RETRIES) {
                    kotlinx.coroutines.delay(RETRY_DELAY_MS * retryCount)
                }
            }
        }

        throw lastException ?: WeatherException.ApiException.InvalidApiKey("Failed to fetch API key after $MAX_RETRIES attempts")
    }
} 
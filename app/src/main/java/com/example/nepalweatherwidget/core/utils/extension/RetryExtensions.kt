package com.example.nepalweatherwidget.core.extension

import kotlinx.coroutines.delay
import kotlin.math.pow

suspend fun <T> withRetry(
    maxAttempts: Int = 3,
    initialDelayMillis: Long = 1000,
    maxDelayMillis: Long = 10000,
    factor: Double = 2.0,
    shouldRetry: (Exception) -> Boolean = { true },
    block: suspend () -> T
): T {
    var currentDelay = initialDelayMillis
    repeat(maxAttempts - 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (!shouldRetry(e) || attempt == maxAttempts - 2) {
                throw e
            }
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelayMillis)
        }
    }
    return block() // Last attempt
}

// Specific retry for network operations
suspend fun <T> withNetworkRetry(
    maxAttempts: Int = 3,
    block: suspend () -> T
): T = withRetry(
    maxAttempts = maxAttempts,
    initialDelayMillis = 1000,
    shouldRetry = { e ->
        e is java.net.UnknownHostException ||
        e is java.net.SocketTimeoutException ||
        e is java.io.IOException
    },
    block = block
) 
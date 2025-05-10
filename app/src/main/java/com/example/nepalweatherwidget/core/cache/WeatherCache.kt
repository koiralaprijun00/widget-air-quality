package com.example.nepalweatherwidget.core.cache

import com.example.nepalweatherwidget.core.result.Result
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

@Singleton
class WeatherCache @Inject constructor() {
    
    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()
    private val mutex = Mutex()
    
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long
    )
    
    suspend fun <T> getOrFetch(
        key: String,
        validity: Duration,
        fetcher: suspend () -> Result<T>
    ): Result<T> = mutex.withLock {
        val cached = cache[key] as? CacheEntry<T>
        
        if (cached != null && isValid(cached.timestamp, validity)) {
            return@withLock Result.Success(cached.data)
        }
        
        return@withLock when (val result = fetcher()) {
            is Result.Success -> {
                cache[key] = CacheEntry(result.data, System.currentTimeMillis())
                result
            }
            is Result.Error -> result
        }
    }
    
    private fun isValid(timestamp: Long, validity: Duration): Boolean {
        return System.currentTimeMillis() - timestamp < validity.inWholeMilliseconds
    }
    
    fun invalidate(key: String) {
        cache.remove(key)
    }
    
    fun invalidateAll() {
        cache.clear()
    }
    
    fun getCachedData<T>(key: String): T? {
        return (cache[key] as? CacheEntry<T>)?.data
    }
    
    fun isDataValid(key: String, validity: Duration): Boolean {
        val entry = cache[key] ?: return false
        return isValid(entry.timestamp, validity)
    }
} 
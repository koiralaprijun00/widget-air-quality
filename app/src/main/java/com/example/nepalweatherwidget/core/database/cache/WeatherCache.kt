package com.example.nepalweatherwidget.core.cache

import com.example.nepalweatherwidget.core.result.Result
import javax.inject.Singleton
import javax.inject.Inject
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

@Singleton
class WeatherCache @Inject constructor() {
    
    private val cache = ConcurrentHashMap<String, CacheEntry<*>>()
    private val mutex = Mutex()
    private val maxEntries = 100
    
    data class CacheEntry<T>(
        val data: T,
        val timestamp: Long,
        val lastAccessed: Long = System.currentTimeMillis()
    )
    
    suspend fun <T> getOrFetch(
        key: String,
        validity: Duration,
        fetcher: suspend () -> Result<T>
    ): Result<T> = mutex.withLock {
        evictIfNeeded()
        
        val cached = cache[key] as? CacheEntry<T>
        
        if (cached != null && isValid(cached.timestamp, validity)) {
            // Update last accessed time
            cache[key] = cached.copy(lastAccessed = System.currentTimeMillis())
            return@withLock Result.Success(cached.data)
        }
        
        return@withLock when (val result = fetcher()) {
            is Result.Success -> {
                cache[key] = CacheEntry(
                    data = result.data,
                    timestamp = System.currentTimeMillis(),
                    lastAccessed = System.currentTimeMillis()
                )
                result
            }
            is Result.Error -> result
        }
    }
    
    private fun isValid(timestamp: Long, validity: Duration): Boolean {
        return System.currentTimeMillis() - timestamp < validity.inWholeMilliseconds
    }
    
    private fun evictIfNeeded() {
        if (cache.size >= maxEntries) {
            // Remove 20% of the oldest accessed entries
            val entriesToRemove = (maxEntries * 0.2).toInt()
            cache.entries
                .sortedBy { it.value.lastAccessed }
                .take(entriesToRemove)
                .forEach { cache.remove(it.key) }
        }
    }
    
    fun invalidate(key: String) {
        cache.remove(key)
    }
    
    fun invalidateAll() {
        cache.clear()
    }
    
    fun <T> getCachedData(key: String): T? {
        return (cache[key] as? CacheEntry<T>)?.data
    }
    
    fun isDataValid(key: String, validity: Duration): Boolean {
        val entry = cache[key] ?: return false
        return isValid(entry.timestamp, validity)
    }
    
    fun size(): Int = cache.size
}
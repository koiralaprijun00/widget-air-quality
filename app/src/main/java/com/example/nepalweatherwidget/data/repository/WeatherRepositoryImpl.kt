package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.core.util.ApiResult
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.local.entity.AirQualityEntity
import com.example.nepalweatherwidget.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.remote.api.WeatherService
import com.example.nepalweatherwidget.data.remote.mapper.AirQualityMapper
import com.example.nepalweatherwidget.data.remote.mapper.WeatherMapper
import com.example.nepalweatherwidget.domain.exception.WeatherException
import com.example.nepalweatherwidget.domain.model.AirQuality
import com.example.nepalweatherwidget.domain.model.ApiResult
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.network.NetworkMonitor
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val airPollutionService: AirPollutionService,
    private val networkMonitor: NetworkMonitor,
    private val weatherDao: WeatherDao,
    private val airQualityDao: AirQualityDao,
    private val apiKey: String
) : WeatherRepository {

    override suspend fun getWeatherAndAirQuality(location: String): ApiResult<Pair<WeatherData, AirQuality>> {
        return withContext(Dispatchers.IO) {
            if (!networkMonitor.isNetworkAvailable()) {
                // Try to get cached data when offline
                val cachedWeather = weatherDao.getWeatherData(location)
                val cachedAirQuality = airQualityDao.getAirQualityData(location)
                
                if (cachedWeather != null && cachedAirQuality != null) {
                    return@withContext ApiResult.Success(
                        Pair(
                            cachedWeather.toWeatherData(),
                            cachedAirQuality.toAirQualityData()
                        )
                    )
                }
                return@withContext ApiResult.Error(WeatherException.NetworkError("No internet connection available"))
            }

            try {
                // For now, using hardcoded Kathmandu coordinates
                // TODO: Implement proper geocoding
                val lat = 27.7172
                val lon = 85.3240

                val weatherResult = getWeatherData(lat, lon)
                val airQualityResult = getAirQuality(lat, lon)

                when {
                    weatherResult is ApiResult.Error -> weatherResult
                    airQualityResult is ApiResult.Error -> airQualityResult
                    weatherResult is ApiResult.Success && airQualityResult is ApiResult.Success -> {
                        // Cache the data
                        weatherDao.insertWeatherData(
                            WeatherEntity(
                                location = location,
                                temperature = weatherResult.data.temperature,
                                feelsLike = weatherResult.data.feelsLike,
                                description = weatherResult.data.description,
                                iconCode = weatherResult.data.iconCode,
                                humidity = weatherResult.data.humidity,
                                windSpeed = weatherResult.data.windSpeed,
                                timestamp = weatherResult.data.timestamp
                            )
                        )
                        airQualityDao.insertAirQualityData(
                            AirQualityEntity(
                                location = location,
                                aqi = airQualityResult.data.aqi,
                                pm25 = airQualityResult.data.pm25,
                                pm10 = airQualityResult.data.pm10,
                                timestamp = airQualityResult.data.timestamp
                            )
                        )
                        ApiResult.Success(Pair(weatherResult.data, airQualityResult.data))
                    }
                    else -> ApiResult.Error(WeatherException.UnknownError("Failed to get weather data"))
                }
            } catch (e: Exception) {
                ApiResult.Error(WeatherException.UnknownError(e.message ?: "Unknown error occurred"))
            }
        }
    }

    override suspend fun getWeatherData(lat: Double, lon: Double): ApiResult<WeatherData> {
        return withContext(Dispatchers.IO) {
            if (!networkMonitor.isNetworkAvailable()) {
                return@withContext ApiResult.Error(WeatherException.NetworkError("No internet connection available"))
            }

            try {
                val response = weatherService.getCurrentWeather(lat, lon, apiKey)
                ApiResult.Success(response.toWeatherData())
            } catch (e: HttpException) {
                ApiResult.Error(WeatherException.ApiError(e.code(), e.message()))
            } catch (e: IOException) {
                ApiResult.Error(WeatherException.NetworkError(e.message ?: "Network error occurred"))
            } catch (e: Exception) {
                ApiResult.Error(WeatherException.UnknownError(e.message ?: "Unknown error occurred"))
            }
        }
    }

    override suspend fun getAirQuality(lat: Double, lon: Double): ApiResult<AirQuality> {
        return withContext(Dispatchers.IO) {
            if (!networkMonitor.isNetworkAvailable()) {
                return@withContext ApiResult.Error(WeatherException.NetworkError("No internet connection available"))
            }

            try {
                val response = airPollutionService.getCurrentAirQuality(lat, lon, apiKey)
                if (response.list.isNotEmpty()) {
                    ApiResult.Success(response.list[0].toAirQuality())
                } else {
                    ApiResult.Error(WeatherException.DataError("No air quality data available"))
                }
            } catch (e: HttpException) {
                ApiResult.Error(WeatherException.ApiError(e.code(), e.message()))
            } catch (e: IOException) {
                ApiResult.Error(WeatherException.NetworkError(e.message ?: "Network error occurred"))
            } catch (e: Exception) {
                ApiResult.Error(WeatherException.UnknownError(e.message ?: "Unknown error occurred"))
            }
        }
    }

    private fun WeatherEntity.toWeatherData() = WeatherData(
        temperature = temperature,
        feelsLike = feelsLike,
        description = description,
        iconCode = iconCode,
        humidity = humidity,
        windSpeed = windSpeed,
        timestamp = timestamp
    )

    private fun AirQualityEntity.toAirQualityData() = AirQuality(
        aqi = aqi,
        pm25 = pm25,
        pm10 = pm10,
        timestamp = timestamp
    )
} 
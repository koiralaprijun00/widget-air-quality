package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.BuildConfig
import com.example.nepalweatherwidget.data.local.dao.AirQualityDao
import com.example.nepalweatherwidget.data.local.dao.WeatherDao
import com.example.nepalweatherwidget.data.local.entity.AirQualityEntity
import com.example.nepalweatherwidget.data.local.entity.WeatherEntity
import com.example.nepalweatherwidget.data.remote.api.AirPollutionService
import com.example.nepalweatherwidget.data.remote.api.WeatherService
import com.example.nepalweatherwidget.domain.exception.WeatherException
import com.example.nepalweatherwidget.domain.model.AirQualityData
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

class WeatherRepositoryImpl @Inject constructor(
    private val weatherService: WeatherService,
    private val airPollutionService: AirPollutionService,
    private val networkMonitor: NetworkMonitor,
    private val weatherDao: WeatherDao,
    private val airQualityDao: AirQualityDao
) : WeatherRepository {

    override suspend fun getWeatherAndAirQuality(location: String): ApiResult<Pair<WeatherData, AirQualityData>> {
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
                when (e) {
                    is HttpException -> ApiResult.Error(WeatherException.ApiError(e.code(), e.message()))
                    is IOException -> ApiResult.Error(WeatherException.NetworkError(e.message))
                    else -> ApiResult.Error(WeatherException.UnknownError(e.message))
                }
            }
        }
    }

    override suspend fun getWeatherData(lat: Double, lon: Double): ApiResult<WeatherData> {
        return withContext(Dispatchers.IO) {
            if (!networkMonitor.isNetworkAvailable()) {
                return@withContext ApiResult.Error(WeatherException.NetworkError("No internet connection available"))
            }

            try {
                val response = weatherService.getCurrentWeather(
                    lat = lat,
                    lon = lon,
                    apiKey = BuildConfig.OPENWEATHER_API_KEY
                )

                if (response.weather.isEmpty()) {
                    return@withContext ApiResult.Error(WeatherException.DataError("No weather data available"))
                }

                val weather = response.weather.first()
                val weatherData = WeatherData(
                    temperature = response.main.temp,
                    feelsLike = response.main.feelsLike,
                    description = weather.description,
                    iconCode = weather.icon,
                    humidity = response.main.humidity,
                    windSpeed = response.wind.speed,
                    timestamp = response.timestamp
                )

                ApiResult.Success(weatherData)
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> ApiResult.Error(WeatherException.ApiError(e.code(), e.message()))
                    is IOException -> ApiResult.Error(WeatherException.NetworkError(e.message))
                    else -> ApiResult.Error(WeatherException.UnknownError(e.message))
                }
            }
        }
    }

    override suspend fun getAirQuality(lat: Double, lon: Double): ApiResult<AirQualityData> {
        return withContext(Dispatchers.IO) {
            if (!networkMonitor.isNetworkAvailable()) {
                return@withContext ApiResult.Error(WeatherException.NetworkError("No internet connection available"))
            }

            try {
                val response = airPollutionService.getCurrentAirQuality(
                    lat = lat,
                    lon = lon,
                    apiKey = BuildConfig.OPENWEATHER_API_KEY
                )

                if (response.airQualityList.isEmpty()) {
                    return@withContext ApiResult.Error(WeatherException.DataError("No air quality data available"))
                }

                val airQuality = response.airQualityList.first()
                val airQualityData = AirQualityData(
                    aqi = airQuality.main.aqi,
                    pm25 = airQuality.components.pm25,
                    pm10 = airQuality.components.pm10,
                    timestamp = airQuality.timestamp
                )

                ApiResult.Success(airQualityData)
            } catch (e: Exception) {
                when (e) {
                    is HttpException -> ApiResult.Error(WeatherException.ApiError(e.code(), e.message()))
                    is IOException -> ApiResult.Error(WeatherException.NetworkError(e.message))
                    else -> ApiResult.Error(WeatherException.UnknownError(e.message))
                }
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

    private fun AirQualityEntity.toAirQualityData() = AirQualityData(
        aqi = aqi,
        pm25 = pm25,
        pm10 = pm10,
        timestamp = timestamp
    )
} 
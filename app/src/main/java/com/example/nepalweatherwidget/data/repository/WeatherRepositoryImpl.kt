package com.example.nepalweatherwidget.data.repository

import com.example.nepalweatherwidget.core.error.ErrorHandler
import com.example.nepalweatherwidget.core.error.WeatherException
import com.example.nepalweatherwidget.core.extension.withNetworkRetry
import com.example.nepalweatherwidget.core.monitor.NetworkMonitor
import com.example.nepalweatherwidget.core.result.Result
import com.example.nepalweatherwidget.core.util.Logger
import com.example.nepalweatherwidget.data.api.WeatherService
import com.example.nepalweatherwidget.data.api.AirPollutionService
import com.example.nepalweatherwidget.data.db.dao.WeatherDao
import com.example.nepalweatherwidget.data.db.dao.AirQualityDao
import com.example.nepalweatherwidget.data.db.entity.WeatherEntity
import com.example.nepalweatherwidget.data.db.entity.AirQualityEntity
import com.example.nepalweatherwidget.data.remote.model.WeatherResponse
import com.example.nepalweatherwidget.data.remote.model.AirQualityResponse
import com.example.nepalweatherwidget.domain.model.WeatherData
import com.example.nepalweatherwidget.domain.model.AirQualityData
import com.example.nepalweatherwidget.domain.repository.WeatherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
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
    private val errorHandler: ErrorHandler,
    @Named("openweather_api_key") private val apiKey: String
) : WeatherRepository {
    
    override fun getWeatherData(location: String): Flow<Result<WeatherData>> = flow {
        try {
            // Check network availability
            if (!networkMonitor.isOnline()) {
                Logger.w("WeatherRepository: Network unavailable, using cached data")
                val cached = weatherDao.getLatestWeatherData().first()
                if (cached != null) {
                    emit(Result.Success(cached.toWeatherData()))
                } else {
                    emit(Result.Error(WeatherException.NetworkError("No internet connection and no cached data available")))
                }
                return@flow
            }

            // Try to get cached data first
            val cached = weatherDao.getLatestWeatherData().first()
            if (cached != null) {
                emit(Result.Success(cached.toWeatherData()))
            }

            // Fetch fresh data with retry
            val response = withNetworkRetry {
                weatherService.getWeatherData(location)
            }

            // Save to database
            val weatherEntity = WeatherEntity.fromResponse(response)
            weatherDao.insertWeatherData(weatherEntity)

            emit(Result.Success(weatherEntity.toWeatherData()))
        } catch (e: Exception) {
            Logger.e("WeatherRepository: Error fetching weather data", e)
            emit(errorHandler.handleException(e))
        }
    }
    
    override fun getAirQuality(location: String): Flow<Result<AirQualityData>> = flow {
        try {
            // Check network availability
            if (!networkMonitor.isOnline()) {
                Logger.w("WeatherRepository: Network unavailable, using cached air quality data")
                val cached = airQualityDao.getLatestAirQualityData().first()
                if (cached != null) {
                    emit(Result.Success(cached.toAirQualityData()))
                } else {
                    emit(Result.Error(WeatherException.NetworkError("No internet connection and no cached air quality data available")))
                }
                return@flow
            }

            // Try to get cached data first
            val cached = airQualityDao.getLatestAirQualityData().first()
            if (cached != null) {
                emit(Result.Success(cached.toAirQualityData()))
            }

            // Fetch fresh data with retry
            val response = withNetworkRetry {
                airPollutionService.getAirQualityData(location)
            }

            // Save to database
            val airQualityEntity = AirQualityEntity.fromResponse(response)
            airQualityDao.insertAirQualityData(airQualityEntity)

            emit(Result.Success(airQualityEntity.toAirQualityData()))
        } catch (e: Exception) {
            Logger.e("WeatherRepository: Error fetching air quality data", e)
            emit(errorHandler.handleException(e))
        }
    }
    
    override suspend fun getWeatherAndAirQuality(location: String): Result<Pair<WeatherData, AirQualityData>> {
        return try {
            val weatherResult = getWeatherData(location).first()
            val airQualityResult = getAirQuality(location).first()
            
            when {
                weatherResult is Result.Success && airQualityResult is Result.Success -> {
                    Result.Success(Pair(weatherResult.data, airQualityResult.data))
                }
                weatherResult is Result.Error -> weatherResult
                airQualityResult is Result.Error -> airQualityResult
                else -> Result.Error(WeatherException.UnknownError())
            }
        } catch (e: Exception) {
            Logger.e("WeatherRepository: Error fetching combined data", e)
            errorHandler.handleException(e)
        }
    }
} 
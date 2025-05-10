override suspend fun getWeatherByLocationName(locationName: String): Result<WeatherData> {
    return try {
        val geocodingResult = geocodingService.getCoordinates(locationName)
        if (geocodingResult.isSuccess) {
            val coordinates = geocodingResult.getOrNull()
            if (coordinates != null) {
                getWeatherByCoordinates(coordinates.latitude, coordinates.longitude)
            } else {
                Result.failure(Exception("Failed to get coordinates for location: $locationName"))
            }
        } else {
            Result.failure(geocodingResult.exceptionOrNull() ?: Exception("Failed to get coordinates for location: $locationName"))
        }
    } catch (e: Exception) {
        Result.failure(e)
    }
} 
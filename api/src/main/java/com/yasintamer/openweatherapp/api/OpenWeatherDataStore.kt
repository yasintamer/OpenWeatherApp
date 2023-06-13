package com.yasintamer.openweatherapp.api

import com.yasintamer.openweatherapp.api.entity.WeatherLocation
import kotlinx.coroutines.flow.Flow

interface OpenWeatherDataStore {
    suspend fun editCurrentLocation(weatherLocation: WeatherLocation)
    fun getCurrentLocation(): Flow<WeatherLocation?>
}

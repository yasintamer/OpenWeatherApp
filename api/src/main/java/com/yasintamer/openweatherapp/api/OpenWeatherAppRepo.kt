package com.yasintamer.openweatherapp.api

import com.yasintamer.openweatherapp.api.entity.CurrentWeather
import com.yasintamer.openweatherapp.api.entity.WeatherLocation

interface OpenWeatherAppRepo {
    suspend fun getLocations(query: String): Result<List<WeatherLocation>>
    suspend fun getLocationByGps(lat: Double, lon: Double): Result<WeatherLocation?>
    suspend fun getCurrentWeather(location: WeatherLocation): Result<CurrentWeather>
}

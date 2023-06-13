package com.yasintamer.openweatherapp.data

import com.yasintamer.openweatherapp.api.OpenWeatherAppRepo
import com.yasintamer.openweatherapp.api.entity.CurrentWeather
import com.yasintamer.openweatherapp.api.entity.WeatherLocation
import javax.inject.Inject

private const val API_KEY = "a90bb050b3725ac8fd276052cfa0168a"

class OpenWeatherAppRepoImpl @Inject constructor(
    private val openWeatherApi: OpenWeatherApi
): OpenWeatherAppRepo {
    override suspend fun getLocations(query: String): Result<List<WeatherLocation>> = runCatching {
        val result = openWeatherApi.getLocationByCityName(query, 5, API_KEY)
        result.filter {it.state != null && it.country != null && it.country == "US"}.map {
            it.toWeatherLocation()
        }
    }

    override suspend fun getLocationByGps(lat: Double, lon: Double) = runCatching {
        val result = openWeatherApi.getLocationByGps(lat, lon, 5, API_KEY)
        result.filter { it.state != null && it.country != null && it.country == "US"}.getOrNull(0)?.toWeatherLocation()
    }

    override suspend fun getCurrentWeather(location: WeatherLocation): Result<CurrentWeather> = runCatching {
        openWeatherApi.getWeather(location.lat, location.lon, API_KEY).toCurrentWeather()
    }
}
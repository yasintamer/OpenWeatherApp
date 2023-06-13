package com.yasintamer.openweatherapp.data.dto

import com.yasintamer.openweatherapp.api.entity.WeatherLocation
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationDto(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String?,
    val state: String?
) {
    fun toWeatherLocation(): WeatherLocation = WeatherLocation(name, lat, lon, state)
}

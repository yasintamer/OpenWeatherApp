package com.yasintamer.openweatherapp.api.entity

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherLocation(
    val name: String,
    val lat: Double,
    val lon: Double,
    val state: String?,
)

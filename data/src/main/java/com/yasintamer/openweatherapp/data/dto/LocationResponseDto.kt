package com.yasintamer.openweatherapp.data.dto

import com.yasintamer.openweatherapp.data.dto.LocationDto
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LocationResponseDto(
    val data: List<LocationDto>? = null,
    val code: String? = null,
    val message: String? = null
)

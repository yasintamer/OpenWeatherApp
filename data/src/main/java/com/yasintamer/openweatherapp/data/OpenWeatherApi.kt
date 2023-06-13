package com.yasintamer.openweatherapp.data

import com.yasintamer.openweatherapp.data.dto.LocationDto
import com.yasintamer.openweatherapp.data.dto.WeatherResponseDto
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenWeatherApi {

    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): WeatherResponseDto

    @GET("geo/1.0/direct")
    suspend fun getLocationByCityName(
        @Query("q") cityName: String,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): List<LocationDto>

    @GET("geo/1.0/reverse")
    suspend fun getLocationByGps(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("limit") limit: Int,
        @Query("appid") apiKey: String
    ): List<LocationDto>

}
package com.yasintamer.openweatherapp.api.entity

data class CurrentWeather(
    val mainData: MainData?,
    val weatherData: WeatherData?,
    val windData: WindData?,
    val sysData: SysData?,
    val couldLevel: Long,
)

data class WeatherData(
    val main: String,
    val description: String,
    val icon: String,
)

data class MainData(
    val temp: Double,
    val feelsLike: Double,
    val tempMin: Double,
    val tempMax: Double,
    val pressure: Long,
    val humidity: Long,
)

data class WindData(
    val speed: Double,
    val deg: Long,
    val gust: Double?,
)

data class SysData(
    val sunrise: Long,
    val sunset: Long,
)

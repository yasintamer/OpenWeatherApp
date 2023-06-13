package com.yasintamer.openweatherapp.data.dto

import com.yasintamer.openweatherapp.api.entity.CurrentWeather
import com.yasintamer.openweatherapp.api.entity.MainData
import com.yasintamer.openweatherapp.api.entity.SysData
import com.yasintamer.openweatherapp.api.entity.WeatherData
import com.yasintamer.openweatherapp.api.entity.WindData
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class WeatherResponseDto (
    var coord: CoordDto,
    var weather: List<WeatherDto> = arrayListOf(),
    var base: String,
    var main: MainDto,
    var visibility: Int? = null,
    var wind: WindDto,
    var clouds: CloudsDto,
    var dt: Int,
    var sys: SysDto,
    var timezone: Int,
    var id: Int,
    var name: String,
    var cod: Int
) {
    fun toCurrentWeather(): CurrentWeather = CurrentWeather(
        main.toMainData(),
        weather.getOrNull(0)?.toWeatherData(),
        wind.toWindData(),
        sys.toSysData(),
        clouds.all
    )
}

@JsonClass(generateAdapter = true)
data class CloudsDto (
    val all: Long
)

@JsonClass(generateAdapter = true)
data class CoordDto (
    val lon: Double,
    val lat: Double
)

@JsonClass(generateAdapter = true)
data class MainDto (
    val temp: Double,

    @Json(name = "feels_like")
    val feelsLike: Double,

    @Json(name = "temp_min")
    val tempMin: Double,

    @Json(name = "temp_max")
    val tempMax: Double,

    val pressure: Long,
    val humidity: Long
) {
    fun toMainData() = MainData(temp, feelsLike, tempMin, tempMax, pressure, humidity)
}

@JsonClass(generateAdapter = true)
data class SysDto (
    val type: Long,
    val id: Long,
    val country: String,
    val sunrise: Long,
    val sunset: Long
) {
    fun toSysData() = SysData(sunrise, sunset)
}

@JsonClass(generateAdapter = true)
data class WeatherDto (
    val id: Long,
    val main: String,
    val description: String,
    val icon: String
) {
    fun toWeatherData() = WeatherData(main, description, icon)
}

@JsonClass(generateAdapter = true)
data class WindDto (
    val speed: Double,
    val deg: Long,
    val gust: Double?
) {
    fun toWindData() = WindData(speed, deg, gust)
}

package com.yasintamer.openweatherapp.data

import com.yasintamer.openweatherapp.api.LatLonLocation
import com.yasintamer.openweatherapp.api.OpenWeatherAppRepo
import com.yasintamer.openweatherapp.api.OpenWeatherDataStore
import com.yasintamer.openweatherapp.api.UseCase
import com.yasintamer.openweatherapp.api.entity.WeatherLocation
import javax.inject.Inject

class GetLocationByGpsAndPersistUseCase @Inject constructor(
    private val weatherAppRepo: OpenWeatherAppRepo,
    private val openWeatherDataStore: OpenWeatherDataStore
): UseCase<LatLonLocation, WeatherLocation?> {

    override suspend fun execute(param: LatLonLocation): WeatherLocation? {
        return weatherAppRepo.getLocationByGps(param.lat, param.lon).getOrNull()?.let {
            openWeatherDataStore.editCurrentLocation(it)
            it
        }
    }
}
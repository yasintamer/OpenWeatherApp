package com.yasintamer.openweatherapp.presentation

import android.location.Location
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yasintamer.openweatherapp.api.LatLonLocation
import com.yasintamer.openweatherapp.api.OpenWeatherAppRepo
import com.yasintamer.openweatherapp.api.OpenWeatherDataStore
import com.yasintamer.openweatherapp.api.UseCase
import com.yasintamer.openweatherapp.api.entity.CurrentWeather
import com.yasintamer.openweatherapp.api.entity.WeatherLocation
import com.yasintamer.openweatherapp.api.invoke
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class WeatherHomeViewModel @Inject constructor(
    private val weatherAppRepo: OpenWeatherAppRepo,
    private val useCase: UseCase<LatLonLocation, WeatherLocation?>,
    private val openWeatherDataStore: OpenWeatherDataStore,
) : ViewModel() {

    val isLookupVisible: LiveData<Boolean>
        get() = mutableIsLookupVisible

    val locationsViewState: LiveData<LocationsViewState>
        get() = mutableLocationsViewState

    val currentWeatherViewState: LiveData<CurrentWeatherViewState>
        get() = mutableCurrentWeatherViewState

    private var isGpsLocationInProgress: MutableStateFlow<Boolean> = MutableStateFlow(false)

    val lastLocation: Flow<WeatherLocation?> = openWeatherDataStore.getCurrentLocation().combine(isGpsLocationInProgress) { lastLocation, isGpsInProgress ->
        if (!isGpsInProgress) {
            lastLocation?.let { location ->
                getWeather(location)
                currentLocation = location
                Timber.d("last location changed: ${location.lat} ${location.lon} ${location.name}")
            }
            lastLocation
        } else {
            lastWeatherJob?.cancel()
            null
        }
    }

    private var currentLocation: WeatherLocation? = null

    private var lastQuery: String? = null
    private var lastFindLocationJob: Job? = null
    private var lastWeatherJob: Job? = null

    private val mutableIsLookupVisible = MutableLiveData(true)
    private val mutableLocationsViewState: MutableLiveData<LocationsViewState> = MutableLiveData(LocationsViewState.Initial)
    private val mutableCurrentWeatherViewState: MutableLiveData<CurrentWeatherViewState> = MutableLiveData()

    fun findLocations(query: String) {
        if (lastQuery != query && query.isNotBlank()) {
            lastQuery = query
            lastFindLocationJob?.cancel()
            lastFindLocationJob = viewModelScope.launch {
                delay(500)
                mutableLocationsViewState.value = LocationsViewState.Loading
                weatherAppRepo.getLocations(query)
                    .onSuccess {
                        mutableLocationsViewState.value = LocationsViewState.Success(it)
                    }.onFailure {
                        if (it !is CancellationException) {
                            Timber.e(it)
                            mutableLocationsViewState.value = LocationsViewState.Error(it.message)
                        }
                    }
            }
        }
    }

    private fun getWeather(location: WeatherLocation) {
        closeLookupAndResetState()
        lastWeatherJob = viewModelScope.launch {
            mutableCurrentWeatherViewState.value = CurrentWeatherViewState.Loading
            weatherAppRepo.getCurrentWeather(location)
                .onSuccess {
                    openWeatherDataStore.editCurrentLocation(location)
                    mutableCurrentWeatherViewState.value = CurrentWeatherViewState.Success(it)
                }.onFailure {
                    if (it !is CancellationException) {
                        Timber.e(it)
                        mutableCurrentWeatherViewState.value = CurrentWeatherViewState.Error(it.message)
                    }
                }
        }
    }

    private fun closeLookupAndResetState() {
        mutableIsLookupVisible.value = false
        mutableLocationsViewState.value = LocationsViewState.Initial
    }

    fun openLookupPage() {
        mutableIsLookupVisible.value = true
    }

    fun findAndSaveCurrentLocation(location: Location) {
        viewModelScope.launch {
            isGpsLocationInProgress.value = true
            useCase(LatLonLocation(location.latitude, location.longitude))
            isGpsLocationInProgress.value = false
        }
    }

    fun setLastLocation(weatherLocation: WeatherLocation) {
        if (weatherLocation == currentLocation) {
            closeLookupAndResetState()
        } else {
            viewModelScope.launch {
                openWeatherDataStore.editCurrentLocation(weatherLocation)
            }
        }
    }
}

sealed class LocationsViewState {
    object Initial : LocationsViewState()
    object Loading : LocationsViewState()
    data class Success(val data: List<WeatherLocation>) : LocationsViewState()
    data class Error(val message: String?) : LocationsViewState()
}

sealed class CurrentWeatherViewState {
    object Loading : CurrentWeatherViewState()
    data class Success(val data: CurrentWeather) : CurrentWeatherViewState()
    data class Error(val message: String?) : CurrentWeatherViewState()
}

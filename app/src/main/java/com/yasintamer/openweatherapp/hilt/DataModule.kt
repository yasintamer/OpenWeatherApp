package com.yasintamer.openweatherapp.hilt

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yasintamer.openweatherapp.api.LatLonLocation
import com.yasintamer.openweatherapp.api.OpenWeatherAppRepo
import com.yasintamer.openweatherapp.api.OpenWeatherDataStore
import com.yasintamer.openweatherapp.api.UseCase
import com.yasintamer.openweatherapp.api.entity.WeatherLocation
import com.yasintamer.openweatherapp.data.GetLocationByGpsAndPersistUseCase
import com.yasintamer.openweatherapp.data.OpenWeatherApi
import com.yasintamer.openweatherapp.data.OpenWeatherAppRepoImpl
import com.yasintamer.openweatherapp.hilt.PreferenceKeys.CURRENT_LOCATION
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import retrofit2.Retrofit
import javax.inject.Singleton

const val OPENWEATHERAPP_DATA_STORE = "OPENWEATHERAPP_DATA_STORE"
private object PreferenceKeys {
    val CURRENT_LOCATION = stringPreferencesKey("weather_current_location")
}

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    abstract fun providesRepository(weatherAppRepo: OpenWeatherAppRepoImpl): OpenWeatherAppRepo

    @Binds
    abstract fun providesGetLocationByGpsAndPersistUseCase(qetLocationByGpsAndPersistUseCase: GetLocationByGpsAndPersistUseCase): UseCase<LatLonLocation, WeatherLocation?>

    companion object {

        private val Context.openWeatherAppDataStore: DataStore<Preferences> by preferencesDataStore(OPENWEATHERAPP_DATA_STORE)

        @Singleton
        @Provides
        fun provideOpenWeatherDataStore(@ApplicationContext context: Context): OpenWeatherDataStore = object : OpenWeatherDataStore {

            private val moshi = Moshi.Builder().build()

            override suspend fun editCurrentLocation(weatherLocation: WeatherLocation) {
                val adapter: JsonAdapter<WeatherLocation> = moshi.adapter(WeatherLocation::class.java)
                context.openWeatherAppDataStore.edit {
                    it[CURRENT_LOCATION] = adapter.toJson(weatherLocation)
                }
            }

            override fun getCurrentLocation(): Flow<WeatherLocation?> {
                val adapter: JsonAdapter<WeatherLocation> = moshi.adapter(WeatherLocation::class.java)
                return context.openWeatherAppDataStore.data.map {
                    it[CURRENT_LOCATION]?.let { json ->
                        adapter.fromJson(json)
                    }
                }
            }
        }

        @Singleton
        @Provides
        fun provideApiService(retrofit: Retrofit): OpenWeatherApi = retrofit.create(OpenWeatherApi::class.java)
    }
}

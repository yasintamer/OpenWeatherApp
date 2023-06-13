package com.yasintamer.openweatherapp

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber.*
import timber.log.Timber.Forest.plant

@HiltAndroidApp
class OpenWeatherApp : Application() {
    override fun onCreate() {
        super.onCreate()
        plant(DebugTree())
    }
}

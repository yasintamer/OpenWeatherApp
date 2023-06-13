package com.yasintamer.openweatherapp.presentation

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private val decimalFormat = DecimalFormat("###.##", DecimalFormatSymbols.getInstance(Locale.US)).also {
    it.maximumFractionDigits = 2
    it.minimumFractionDigits = 2
}

fun Double.kTof() = "${decimalFormat.format(this.minus(273.15).times(9 / 5).plus(32))} F°"

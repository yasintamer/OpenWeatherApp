package com.yasintamer.openweatherapp.api

interface UseCase<P, R> {
    suspend fun execute(param: P): R
}

suspend operator fun <P, R> UseCase<P, R>.invoke(param: P): R = execute(param)

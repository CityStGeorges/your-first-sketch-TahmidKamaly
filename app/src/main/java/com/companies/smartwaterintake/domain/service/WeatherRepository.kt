package com.companies.smartwaterintake.domain.service

import com.companies.smartwaterintake.data.WeatherResponse
import com.companies.smartwaterintake.domain.remote.WeatherApi
import javax.inject.Inject

class WeatherRepository @Inject constructor(private val api: WeatherApi) {
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return api.getWeather(lat, lon)
    }
}
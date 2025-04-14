package com.companies.smartwaterintake.domain.remote

import com.companies.smartwaterintake.data.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    @GET("data/2.5/weather")
    suspend fun getWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") appid: String = "86d88c26a56cdd45f6f4743c4caf87f5"
    ): WeatherResponse
}
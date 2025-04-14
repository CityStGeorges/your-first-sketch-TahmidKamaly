package com.companies.smartwaterintake.data

data class WeatherResponse(
    val name: String,         // City name
    val main: Main
)

data class Main(
    val temp: Double,
    val feels_like : Double,
    val humidity : Int,
)
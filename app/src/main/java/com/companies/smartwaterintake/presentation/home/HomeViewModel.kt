package com.companies.smartwaterintake.presentation.home

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.companies.smartwaterintake.data.WeatherResponse
import com.companies.smartwaterintake.domain.service.WeatherRepository
import com.companies.smartwaterintake.ui.utils.HealthConnectUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: WeatherRepository,
) : ViewModel() {

    private val _weatherState = MutableStateFlow<WeatherResponse?>(null)
    val weatherState: StateFlow<WeatherResponse?> = _weatherState

    fun fetchWeather(lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val response = repository.getWeather(lat, lon)
                _weatherState.value = response
                Log.d("Weather", "fetchWeather: $response")
            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Error fetching weather", e)
            }
        }
    }

    private val _steps = MutableStateFlow(0)
    val steps: StateFlow<Int> = _steps.asStateFlow()

    fun loadDailySteps(context: Context, onStepsLoaded: (Int) -> Unit = {}) {
        viewModelScope.launch {
            val zone = ZoneId.systemDefault()
            val todayMidnight = LocalDate.now(zone).atStartOfDay(zone)
            val tomorrowMidnight = todayMidnight.plusDays(1)

            val startTime = todayMidnight.toInstant()
            val endTime = tomorrowMidnight.toInstant()

            val stepsCount = HealthConnectUtils.readStepsByTimeRange(startTime, endTime)
            _steps.value = stepsCount
            Log.d("Steps", "startTime : $startTime, count : $stepsCount, endTime : $endTime")
            onStepsLoaded(stepsCount)
        }
    }
}

package com.companies.smartwaterintake

import android.util.Log
import com.companies.smartwaterintake.data.Cup
import com.companies.smartwaterintake.data.Day
import com.companies.smartwaterintake.data.LiquidUnit
import com.companies.smartwaterintake.data.Milliliters
import com.companies.smartwaterintake.data.Now
import com.companies.smartwaterintake.data.Percent
import com.companies.smartwaterintake.data.Reminder
import com.companies.smartwaterintake.data.Theme
import com.companies.smartwaterintake.data.Today
import com.companies.smartwaterintake.data.defaultCups
import com.companies.smartwaterintake.data.defaultSelectedCups
import com.companies.smartwaterintake.data.sumOfMilliliters
import com.companies.smartwaterintake.domain.service.DateChangedService
import com.companies.smartwaterintake.domain.service.HydrationHistoryStore
import com.companies.smartwaterintake.domain.service.NotificationService
import com.companies.smartwaterintake.domain.service.PreferencesStore
import com.companies.smartwaterintake.domain.service.ReminderAlarmService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

data class AppState(
    val dailyGoal: Milliliters,
    val todayHydration: Milliliters,
    val reminder: Reminder?,
    val theme: Theme,
    val canScheduleAlarms: Boolean,
    val defaultCups: List<Cup>,
    val selectedCups: List<Cup>,
    val appInForeground: Boolean,
    val liquidUnit: LiquidUnit,
) {
    val hydrationProgress: Percent = Percent(
        todayHydration.value / dailyGoal.value.toFloat()
    )
    val dailyGoalReached: Boolean = hydrationProgress.value >= 1f
    val allCups = (defaultCups + selectedCups).distinct().sorted()
}

sealed interface AppAction {
    data class SetDailyGoal(val value: Milliliters) : AppAction
    data class AddHydration(val value: Milliliters) : AppAction
    data class SetReminder(val value: Reminder?) : AppAction
    data object RestartReminder : AppAction
    data class ShowHydrationReminderNotification(val forced: Boolean = false) : AppAction
    data class SetTheme(val value: Theme) : AppAction
    data class SetSelectedCups(val value: List<Cup>) : AppAction
    data class SetAppInForeground(val value: Boolean) : AppAction
    data class SetLiquidUnit(val value: LiquidUnit) : AppAction
    data object DeleteAll : AppAction
    data object ResetToday : AppAction
}

class AppStore(
    private val hydrationHistoryStore: HydrationHistoryStore,
    private val notificationService: NotificationService,
    private val preferencesStore: PreferencesStore,
    private val reminderAlarmService: ReminderAlarmService,
    private val scope: CoroutineScope,
    dateChangedService: DateChangedService
) {
    private val _state = MutableStateFlow(
        kotlin.run {
            val liquidUnit = runBlocking { preferencesStore.liquidUnit.first() }
            AppState(
                dailyGoal = runBlocking {
                    preferencesStore.dailyGoal.first() ?: Milliliters.DAILY_GOAL_DEFAULT
                },
                todayHydration = runBlocking {
                    hydrationHistoryStore.day(Today).first()?.hydration?.sumOfMilliliters()
                        ?: Milliliters.ZERO
                },
                reminder = runBlocking { preferencesStore.reminder.first() },
                theme = runBlocking { preferencesStore.theme.first() },
                canScheduleAlarms = reminderAlarmService.canScheduleAlarms.value,
                liquidUnit = liquidUnit,
                defaultCups = defaultCups(liquidUnit),
                selectedCups = runBlocking {
                    preferencesStore.selectedCups.first().sorted()
                        .ifEmpty { defaultSelectedCups(liquidUnit) }
                },
                appInForeground = true,
            )
        }
    )
    val state: StateFlow<AppState> = _state.asStateFlow()

    init {
        with(preferencesStore) {
            dailyGoal.onEach { milliliters ->
                _state.update {
                    it.copy(dailyGoal = milliliters ?: Milliliters.DAILY_GOAL_DEFAULT)
                }
            }.launchIn(scope)
            reminder.onEach { reminder ->
                _state.update { it.copy(reminder = reminder) }
            }.launchIn(scope)
            theme.onEach { theme ->
                _state.update { it.copy(theme = theme) }
            }.launchIn(scope)

            combine(
                liquidUnit,
                selectedCups
            ) { liquidUnit, selectedCups -> liquidUnit to selectedCups }
                .onEach { (liquidUnit, selectedCups) ->
                    _state.update {
                        it.copy(
                            liquidUnit = liquidUnit,
                            defaultCups = defaultCups(liquidUnit),
                            selectedCups = selectedCups.sorted()
                                .ifEmpty { defaultSelectedCups(liquidUnit) }
                        )
                    }
                }
                .launchIn(scope)
        }

        @Suppress("OPT_IN_USAGE")
        dateChangedService.onChanged
            .flatMapLatest { localDate -> hydrationHistoryStore.day(localDate) }
            .onEach { day ->
                val todayHydration = day?.hydration?.sumOfMilliliters()
                    ?: Milliliters.ZERO
                _state.update { it.copy(todayHydration = todayHydration) }
            }
            .launchIn(scope)

        reminderAlarmService.canScheduleAlarms.onEach { canScheduleAlarms ->
            _state.update { it.copy(canScheduleAlarms = canScheduleAlarms) }
        }.launchIn(scope)
    }

    fun dispatch(action: AppAction) {
        when (action) {
            is AppAction.SetDailyGoal -> scope.launch {
                preferencesStore.setDailyGoal(action.value)
                val day = hydrationHistoryStore.day(Today).first()
                if (day != null) {
                    hydrationHistoryStore.setDay(day.copy(goal = action.value))
                }
            }

            is AppAction.AddHydration -> scope.launch {
                notificationService.cancelHydrationReminderNotification()
                val currentState = _state.value
                val storedDay = hydrationHistoryStore.day(Today).first()
                if (storedDay != null) {
                    val updatedDay = storedDay.copy(
                        hydration = storedDay.hydration + Day.Hydration(action.value, Now),
                        goal = currentState.dailyGoal
                    )
                    hydrationHistoryStore.setDay(updatedDay)
                } else {
                    hydrationHistoryStore.setDay(
                        Day(
                            date = Today,
                            hydration = listOf(Day.Hydration(action.value, Now)),
                            goal = currentState.dailyGoal
                        )
                    )
                }
            }

            is AppAction.SetReminder -> scope.launch {
                if (action.value != null) {
                    reminderAlarmService.setAlarm(action.value)
                } else {
                    reminderAlarmService.clear()
                }
                preferencesStore.setReminder(action.value)
            }

            is AppAction.RestartReminder -> scope.launch {
                val reminder = preferencesStore.reminder.first()
                if (reminder != null) {
                    reminderAlarmService.setAlarm(reminder)
                }
            }

            is AppAction.ShowHydrationReminderNotification -> scope.launch {
                if (action.forced) {
                    val todayMilliliters = hydrationHistoryStore.day(Today).first()
                        ?.hydration
                        ?.sumOfMilliliters()
                        ?: Milliliters.ZERO
                    val liquidUnit = preferencesStore.liquidUnit.first()
                    val selectedCups = preferencesStore.selectedCups.first()
                        .ifEmpty { defaultSelectedCups(liquidUnit) }
                        .sorted()
                    notificationService.showHydrationReminderNotification(
                        todayMilliliters = todayMilliliters,
                        todayProgress = _state.value.hydrationProgress,
                        selectedCups = selectedCups,
                        liquidUnit = liquidUnit
                    )
                }
            }

            is AppAction.SetTheme -> scope.launch {
                preferencesStore.setTheme(action.value)
            }

            is AppAction.SetSelectedCups -> scope.launch {
                preferencesStore.setSelectedCups(action.value)
            }

            is AppAction.DeleteAll -> scope.launch {
                preferencesStore.clear()
                hydrationHistoryStore.clear()
                reminderAlarmService.clear()
                notificationService.clear()
            }

            is AppAction.SetAppInForeground -> _state.update {
                it.copy(appInForeground = action.value)
            }

            is AppAction.ResetToday -> scope.launch {
                val today = hydrationHistoryStore.day(Today).first() ?: return@launch
                hydrationHistoryStore.setDay(today.copy(hydration = emptyList()))
            }

            is AppAction.SetLiquidUnit -> scope.launch {
                preferencesStore.setLiquidUnit(action.value)
            }
        }
    }
}
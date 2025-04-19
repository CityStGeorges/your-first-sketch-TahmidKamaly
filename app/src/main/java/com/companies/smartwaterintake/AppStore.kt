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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.number
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime

enum class DateRangeType {
    WEEKLY, MONTHLY, YEARLY
}

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
    val temperature: Double?, // ← Add this!
    val height: String? = null,
    val weight: String? = null,
    val stepsRecord : Int = 0,
    val hydrationChartData: List<Pair<LocalDate, Int>> = emptyList(),

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
    data class RemoveHydration(val value : Milliliters) : AppAction
    data class SetReminder(val value: Reminder?) : AppAction
    data object RestartReminder : AppAction
    data class ShowHydrationReminderNotification(val forced: Boolean = false) : AppAction
    data class SetTemperature(val value: Double) : AppAction // ← Add this
    data class setStepRecord(val value : Int) : AppAction
    data class SetTheme(val value: Theme) : AppAction
    data class SetSelectedCups(val value: List<Cup>) : AppAction
    data class setWeight(val value : String) : AppAction
    data class setHeight(val value : String) : AppAction
    data class SetAppInForeground(val value: Boolean) : AppAction
    data class SetLiquidUnit(val value: LiquidUnit) : AppAction
    data object DeleteAll : AppAction
    data object ResetToday : AppAction
    data class LoadHydrationChartData(val range: DateRangeType) : AppAction

}

class AppStore(
    private val hydrationHistoryStore: HydrationHistoryStore,
    private val notificationService: NotificationService,
    private val preferencesStore: PreferencesStore,
    private val reminderAlarmService: ReminderAlarmService,
    private val scope: CoroutineScope,
    dateChangedService: DateChangedService,
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
                temperature = null,
                height = runBlocking { preferencesStore.height.first() },
                weight = runBlocking { preferencesStore.weight.first() },

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
            height.onEach { height -> _state.update { it.copy(height = height) }
            }.launchIn(scope)
            weight.onEach { weight -> _state.update { it.copy(weight = weight) }
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

            is AppAction.setWeight -> scope.launch {
                preferencesStore.setWeight(action.value)
            }

            is AppAction.setHeight -> scope.launch {
                preferencesStore.setHeight(action.value)
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

            is AppAction.RemoveHydration -> scope.launch {
                hydrationHistoryStore.removeLatestHydration(Today)
                val updatedDay = hydrationHistoryStore.day(Today).firstOrNull()
                updatedDay?.goal?.let { goal ->
                    _state.update { it.copy(dailyGoal = goal) }
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

            is AppAction.LoadHydrationChartData -> scope.launch {
                val now = Clock.System.now()
                    .toLocalDateTime(TimeZone.currentSystemDefault())
                    .date

                val (startDate, endDate) = when (action.range) {
                    DateRangeType.WEEKLY -> {
                        // Using ISO day number (Monday = 1) to calculate offset correctly
                        val daysToMonday = now.dayOfWeek.isoDayNumber - 1
                        val monday = now.minus(daysToMonday.days)
                        Pair(monday, monday.plus(6.days))
                    }

                    DateRangeType.MONTHLY -> {
                        // Use now.month.number (1-based) to create the first day of the month
                        val firstDay = LocalDate(now.year, now.month.number, 1)
                        val lastDay = firstDay.plus(1.months).minus(1.days)
                        Pair(firstDay, lastDay)
                    }

                    DateRangeType.YEARLY -> {
                        val start = LocalDate(now.year, 1, 1)
                        // Ensure consistency with month numbering
                        val end = LocalDate(now.year, now.month.number, now.dayOfMonth)
                        Pair(start, end)
                    }
                }

                val allDatesInRange =
                    generateDateSequence(startDate, endDate, action.range).toList()

                val rawData = hydrationHistoryStore.getInRange(
                    startDate.toEpochDays(),
                    endDate.toEpochDays(),
                    1000
                )

                val chartData = when (action.range) {
                    DateRangeType.YEARLY -> {
                        // Group by (year, month) using month-start dates
                        val monthlySums = rawData.groupBy { entry ->
                            val date = entry.date
                            // Use date.month.number for proper month conversion
                            LocalDate(date.year, date.month.number, 1)
                        }
                            .mapValues { it.value.sumOf { item -> item.hydration.sumOfMilliliters().value } }

                        // Generate exactly 12 month entries
                        (0..11).map { offset ->
                            val monthStart = startDate.plus(offset, DateTimeUnit.MONTH)
                            monthStart to (monthlySums[monthStart] ?: 0)
                        }
                    }

                    else -> {
                        // Daily exact mapping
                        val grouped = rawData.groupBy { it.date }
                            .mapValues { it.value.sumOf { h -> h.hydration.sumOfMilliliters().value } }

                        allDatesInRange.map { date ->
                            date to (grouped[date] ?: 0)
                        }
                    }
                }

                Log.d("Chart", "dispatch: $chartData")
                _state.update { it.copy(hydrationChartData = chartData) }
            }

            is AppAction.setStepRecord -> scope.launch {
                _state.update { it.copy(stepsRecord = action.value) }
            }

            is AppAction.ShowHydrationReminderNotification -> scope.launch {
                if (action.forced || (_state.value.temperature ?: 0.0) > 20.0 || _state.value.stepsRecord >= 2000) {
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

            is AppAction.SetTemperature -> {
                _state.update { it.copy(temperature = action.value) }
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

    private fun generateDateSequence(
        start: LocalDate,
        end: LocalDate,
        rangeType: DateRangeType,
    ): Sequence<LocalDate> = sequence {
        var current = start
        // Handle different range types with proper increments
        while (current <= end) {
            yield(current)
            current = when (rangeType) {
                DateRangeType.YEARLY -> current.plus(1, DateTimeUnit.MONTH)
                else -> current.plus(1, DateTimeUnit.DAY)
            }
        }
    }

    val Int.days: DatePeriod get() = DatePeriod(days = this)
    val Int.months: DatePeriod get() = DatePeriod(months = this)
}
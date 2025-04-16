package com.companies.smartwaterintake.domain.service

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.companies.smartwaterintake.data.Cup
import com.companies.smartwaterintake.data.LiquidUnit
import com.companies.smartwaterintake.data.Milliliters
import com.companies.smartwaterintake.data.Reminder
import com.companies.smartwaterintake.data.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

class PreferencesStore(
    private val context: Context
) {

    val dailyGoal: Flow<Milliliters?> = context.dataStore.data.map { preferences ->
        val persisted = preferences[dailyTargetMillilitersKey]
        if (persisted != null) Milliliters(persisted) else null
    }

    suspend fun setDailyGoal(milliliters: Milliliters) {
        context.dataStore.edit { it[dailyTargetMillilitersKey] = milliliters.value }
    }

    val reminder: Flow<Reminder?> = context.dataStore.data.map { preferences ->
        val persisted = preferences[reminderKey] ?: return@map null
        json.decodeFromString(persisted)
    }

    suspend fun setReminder(reminder: Reminder?) {
        context.dataStore.edit { preferences ->
            if (reminder == null) {
                preferences.remove(reminderKey)
            } else {
                preferences[reminderKey] = json.encodeToString(reminder)
            }
        }
    }

    val theme: Flow<Theme> = context.dataStore.data.map { preferences ->
        val persisted = preferences[themeKey]
        Theme.of(persisted)
    }

    suspend fun setTheme(theme: Theme) {
        context.dataStore.edit { preferences ->
            preferences[themeKey] = theme.serialized
        }
    }


    val selectedCups: Flow<List<Cup>> = context.dataStore.data.map { preferences ->
        val persisted = preferences[selectedCupsKey] ?: return@map emptyList()
        json.decodeFromString<List<Cup>>(persisted)
    }

    suspend fun setSelectedCups(cups: List<Cup>) {
        context.dataStore.edit { preferences ->
            preferences[selectedCupsKey] = json.encodeToString(cups)
        }
    }

    val liquidUnit: Flow<LiquidUnit> = context.dataStore.data.map { preferences ->
        val persisted = preferences[liquidUnitKey]
        LiquidUnit.of(persisted)
    }

    suspend fun setLiquidUnit(unit: LiquidUnit) {
        context.dataStore.edit { it[liquidUnitKey] = unit.serialized }
    }
    val height: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[heightKey]
    }

    suspend fun setHeight(height: String) {
        context.dataStore.edit { it[heightKey] = height }
    }

    // 🔥 NEW: Save and read weight
    val weight: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[weightKey]
    }

    suspend fun setWeight(weight: String) {
        context.dataStore.edit { it[weightKey] = weight }
    }
    suspend fun clear() {
        context.dataStore.edit(MutablePreferences::clear)
    }

    companion object {
        private val json = Json
        private val Context.dataStore by preferencesDataStore("user_preferences")
        private val dailyTargetMillilitersKey = intPreferencesKey("dailyTargetMilliliters")
        private val reminderKey = stringPreferencesKey("reminder")
        private val themeKey = stringPreferencesKey("theme")
        private val selectedCupsKey = stringPreferencesKey("selectedCups")
        private val liquidUnitKey = stringPreferencesKey("liquidUnit")
        private val heightKey = stringPreferencesKey("height")
        private val weightKey = stringPreferencesKey("weight")
    }
}

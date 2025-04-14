package com.companies.smartwaterintake

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.companies.smartwaterintake.domain.service.AndroidDateChangedService
import com.companies.smartwaterintake.domain.service.Channel
import com.companies.smartwaterintake.domain.service.HydrationHistoryStore
import com.companies.smartwaterintake.domain.service.NotificationService
import com.companies.smartwaterintake.domain.service.PreferencesStore
import com.companies.smartwaterintake.domain.service.ReminderAlarmService
import com.companies.smartwaterintake.domain.service.SqliteHydrationHistoryStore
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class App : Application() {

    lateinit var store: AppStore
    lateinit var hydrationHistoryStore: HydrationHistoryStore

    override fun onCreate() {
        super.onCreate()

        instance = this

        Channel.registerAll(this)

        val processLifecycleOwner = ProcessLifecycleOwner.get()
        hydrationHistoryStore = SqliteHydrationHistoryStore(
            context = applicationContext,
        )

        val preferencesStore = PreferencesStore(
            context = applicationContext
        )
        val notificationService = NotificationService(
            context = applicationContext
        )
        val reminderAlarmService = ReminderAlarmService(
            context = applicationContext,
            preferencesStore = preferencesStore,
            processLifecycleOwner.lifecycleScope
        )
        val dateChangedService = AndroidDateChangedService(
            context = applicationContext
        )
        store = AppStore(
            scope = processLifecycleOwner.lifecycleScope,
            preferencesStore = preferencesStore,
            hydrationHistoryStore = hydrationHistoryStore,
            reminderAlarmService = reminderAlarmService,
            notificationService = notificationService,
            dateChangedService = dateChangedService
        )

        processLifecycleOwner.lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onResume(owner: LifecycleOwner) {
                    store.dispatch(AppAction.SetAppInForeground(true))
                }

                override fun onPause(owner: LifecycleOwner) {
                    store.dispatch(AppAction.SetAppInForeground(false))
                }
            }
        )
    }
    companion object {
        lateinit var instance: App
    }
}

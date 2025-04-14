package com.companies.smartwaterintake.domain.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.getSystemService
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.companies.smartwaterintake.MainActivity
import androidx.annotation.IntRange
import com.companies.smartwaterintake.App
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.R
import com.companies.smartwaterintake.data.Cup
import com.companies.smartwaterintake.data.LiquidUnit
import com.companies.smartwaterintake.data.Milliliters
import com.companies.smartwaterintake.data.Percent
import com.companies.smartwaterintake.data.format

class NotificationService(
    private val context: Context
) {

    private val notificationManager: NotificationManager = checkNotNull(context.getSystemService())

    private val openAppPendingIntent = PendingIntent.getActivity(
        context,
        1,
        Intent(context, MainActivity::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun showHydrationReminderNotification(
        todayMilliliters: Milliliters,
        todayProgress: Percent,
        selectedCups: List<Cup>,
        liquidUnit: LiquidUnit
    ) {
        notificationManager.notify(
            HYDRATION_REMINDER_ID,
            Notification.Builder(
                context,
                Channel.Hydrate.id
            ).apply {
                setSmallIcon(R.drawable.ic_notification)
                setContentTitle("Hydration Reminder")
                setContentText(todayMilliliters.asReminderMessage())
                setSubText("${todayMilliliters.format(liquidUnit)} (${todayProgress.format()})")
                setColor(NOTIFICATION_COLOR)
                setContentIntent(openAppPendingIntent)
                selectedCups.forEach { cup ->
                    addAction(
                        Notification.Action.Builder(
                            null,
                            cup.milliliters.format(liquidUnit),
                            addCupPendingIntent(cup.milliliters)
                        ).build()
                    )
                }
                setShowWhen(true)
                setAutoCancel(true)
            }.build()
        )
    }

    private fun Milliliters.asReminderMessage(): String = when (value) {
        in 0..199 -> "Time to Hydrate! Take a Sip of Water and Stay Refreshed."
        in 200..299 -> "Stay Hydrated! Your Body Needs Water. Take a Break and Drink Up!"
        in 300..399 -> "Hydration Alert! Grab a Glass of Water and Rehydrate."
        in 400..499 -> "Don't Forget to Drink Water! Your Body Thanks You."
        in 500..599 -> "Quench Your Thirst! It's Hydration O'Clock."
        in 600..699 -> "Stay Healthy and Hydrated! Time for a Water Break."
        in 700..799 -> "Water Time! Hydrate Yourself for Optimal Wellness."
        in 800..899 -> "Hydration Check: Have You Had Your Glass of Water Yet?"
        in 900..999 -> "A Little H2O Never Hurt! Stay Hydrated for a Productive Day."
        in 1000..1099 -> "Refill Your Cup! Hydration Is the Key to Feeling Great."
        in 1100..1199 -> "Stay Hydrated! Another Glass of Water Brings You Closer to Wellness."
        in 1200..1299 -> "Hydration Alert! Keep Sipping Water for a Healthy You."
        in 1300..1399 -> "Don't Forget to Stay Hydrated! Your Body Loves Water."
        in 1400..1499 -> "Quench Your Thirst! It's Time for More Hydration."
        in 1500..1599 -> "Stay Healthy and Hydrated! Keep Up the Water Intake."
        in 1600..1699 -> "Water Time! Hydrate to Energize Your Body."
        in 1700..1799 -> "Hydration Check: Keep the Water Coming for a Productive Day."
        in 1800..1899 -> "Stay Hydrated! Your Body Will Thank You."
        in 1900..1999 -> "A Little H2O Never Hurt! Keep Hydrating for Optimal Wellness."
        else -> "Keep Hydrating! Your Body Will Thank You."
    }

    private fun addCupPendingIntent(
        milliliters: Milliliters
    ) = PendingIntent.getBroadcast(
        context,
        milliliters.value,
        Intent(context, AddCupFromNotification::class.java).apply {
            putExtra(AddCupFromNotification.EXTRA_MILLILITERS_VALUE, milliliters.value)
        },
        PendingIntent.FLAG_IMMUTABLE
    )

    fun cancelHydrationReminderNotification() {
        notificationManager.cancel(HYDRATION_REMINDER_ID)
    }



    fun clear() {
        notificationManager.cancelAll()
    }

    companion object {
        private const val HYDRATION_REMINDER_ID = 1357
        private val NOTIFICATION_COLOR = Color(0xFF00A3F8).toArgb()
    }
}

class AddCupFromNotification : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val millilitersValue = intent.getIntExtra(EXTRA_MILLILITERS_VALUE, -1)
        if (millilitersValue == -1) return
        App.instance.store.dispatch(
            AppAction.AddHydration(Milliliters(millilitersValue))
        )
    }

    companion object {
        const val EXTRA_MILLILITERS_VALUE = "EXTRA_MILLILITERS_VALUE"
    }
}

enum class Channel(
    val id: String,
    private val displayName: String,
    private val description: String,
    @IntRange(from = 0, to = 4) private val importance: Int
) {
    Hydrate(
        id = "HydroReminder",
        displayName = "Water Reminder",
        description = "Notifications for water reminders set in the application.",
        importance = NotificationManager.IMPORTANCE_HIGH
    );
    companion object {
        fun registerAll(context: Context) {
            checkNotNull(context.getSystemService<NotificationManager>())
                .createNotificationChannels(
                    entries.map { channel ->
                        NotificationChannel(
                            channel.id,
                            channel.displayName,
                            channel.importance
                        ).also { it.description = channel.description }
                    }
                )
        }
    }
}
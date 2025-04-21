package com.companies.smartwaterintake.presentation.settings

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.NotificationsOff
import androidx.compose.material.icons.outlined.Start
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDefaults
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.R
import com.companies.smartwaterintake.data.Cup
import com.companies.smartwaterintake.data.Reminder
import com.companies.smartwaterintake.data.format
import kotlinx.datetime.LocalTime

@Composable
fun SettingsScreen(
    state: AppState,
    dispatch: (AppAction) -> Unit,
) {
    var themeDialog = remember {
        mutableStateOf(false)
    }

    var liquidUnitDialog = remember {
        mutableStateOf(false)
    }

    var intervalDialog = remember {
        mutableStateOf(false)
    }

    var goalOfTheDayDialog = remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        if (themeDialog.value) {
            ThemeDialog(
                state = state,
                dispatch = dispatch,
                onClose = { themeDialog.value = false }
            )
        }

        if (liquidUnitDialog.value) {
            SetLiquidUnitDialog(
                state = state,
                dispatch = dispatch,
                onClose = { liquidUnitDialog.value = false })
        }

        if (intervalDialog.value) {
            SetIntervalDialog(
                state = state,
                dispatch = dispatch,
                onClose = { intervalDialog.value = false })
        }

        if (goalOfTheDayDialog.value) {
            GoalOfTheDayDialog(
                state = state,
                dispatch = dispatch,
                onClose = { goalOfTheDayDialog.value = false })
        }
        SettingsSection(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            title = "Settings"
        ) {
            SettingItem(
                fieldName = "Daily Goal",
                value = state.dailyGoal.format(state.liquidUnit),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.WaterDrop,
                        contentDescription = null
                    )
                },
                onClick = {
                    goalOfTheDayDialog.value = true
                }
            )
            SettingItem(
                fieldName = "Measurement Unit",
                value = state.liquidUnit.format(),
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_unit),
                        contentDescription = null
                    )
                },
                onClick = { liquidUnitDialog.value = true }
            )
            CupCarouselSelection(
                state = state,
                dispatch = dispatch
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        ReminderSettingsSection(
            canScheduleAlarms = state.canScheduleAlarms,
            reminder = state.reminder,
            onRemindersChanged = { dispatch(AppAction.SetReminder(it)) },
            onSetInterval = { intervalDialog.value = true }
        )

        Spacer(modifier = Modifier.height(16.dp))

        SettingsSection(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            title = "App"
        ) {
            SettingItem(
                fieldName = "Theme",
                value = state.theme.format(),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.InvertColors,
                        contentDescription = null
                    )
                },
                onClick = { themeDialog.value = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))


    }
}


@Composable
private fun ReminderSettingsSection(
    canScheduleAlarms: Boolean,
    reminder: Reminder?,
    onRemindersChanged: (Reminder?) -> Unit,
    onSetInterval: () -> Unit
) {
    var showPermissionRationaleDialog by remember { mutableStateOf(false) }
    var showScheduleAlarmsDialog by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            showPermissionRationaleDialog = true
        } else if (!canScheduleAlarms) {
            showScheduleAlarmsDialog = true
        } else {
            onRemindersChanged(Reminder.DEFAULT)
        }
    }
    val context = LocalContext.current
    SettingsSection(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = "Reminders"
    ) {
        ToggleSettingItem(
            fieldName = "Enabled",
            icon = if (reminder != null) {
                Icons.Outlined.Notifications
            } else {
                Icons.Outlined.NotificationsOff
            },
            checked = reminder != null,
            onCheckedChange = { checked ->
                if (checked) {
                    if (!canScheduleAlarms) {
                        showScheduleAlarmsDialog = true
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val permission = android.Manifest.permission.POST_NOTIFICATIONS
                        if (!context.isPermissionGranted(permission)) {
                            launcher.launch(permission)
                        } else {
                            onRemindersChanged(Reminder.DEFAULT)
                        }
                    } else {
                        onRemindersChanged(Reminder.DEFAULT)
                    }
                } else {
                    onRemindersChanged(null)
                }
            }
        )
        if (reminder != null) {
            var showStartPicker by remember { mutableStateOf(false) }
            var showEndPicker by remember { mutableStateOf(false) }
            var showStartBeforeEndAlert by remember { mutableStateOf(false) }
            SettingItem(
                fieldName = "Start",
                value = reminder.start.format(),
                icon = {
                    Icon(
                        imageVector = Icons.Outlined.Start,
                        contentDescription = null
                    )
                },
                onClick = { showStartPicker = true }
            )
            SettingItem(
                fieldName = "End",
                value = reminder.end.format(),
                icon = {
                    Icon(
                        modifier = Modifier.scale(scaleX = -1f, scaleY = 1f),
                        imageVector = Icons.Outlined.Start,
                        contentDescription = null
                    )
                },
                onClick = { showEndPicker = true }
            )
            SettingItem(
                fieldName = "Interval",
                value = reminder.interval.format(),
                icon = {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_arrow_range),
                        contentDescription = null
                    )
                },
                onClick = onSetInterval
            )
            if (showStartPicker) {
                TimePickerDialog(
                    initialHour = reminder.start.hour,
                    initialMinute = reminder.start.minute,
                    onConfirm = { hour, minute ->
                        showStartPicker = false
                        val start = LocalTime(hour, minute, 0)
                        if (start >= reminder.end) {
                            showStartBeforeEndAlert = true
                        } else {
                            onRemindersChanged(reminder.copy(start = start))
                        }
                    },
                    onCancel = { showStartPicker = false }
                )
            }
            if (showEndPicker) {
                TimePickerDialog(
                    initialHour = reminder.end.hour,
                    initialMinute = reminder.end.minute,
                    onConfirm = { hour, minute ->
                        showEndPicker = false
                        val end = LocalTime(hour, minute, 0)
                        if (reminder.start >= end) {
                            showStartBeforeEndAlert = true
                        } else {
                            onRemindersChanged(reminder.copy(end = end))
                        }
                    },
                    onCancel = { showEndPicker = false }
                )
            }
            if (showStartBeforeEndAlert) {
                StartBeforeEndAlert(
                    onDismiss = { showStartBeforeEndAlert = false }
                )
            }
        }
        if (showPermissionRationaleDialog) {
            NotificationPermissionSettingsAlert(
                onDismiss = { showPermissionRationaleDialog = false }
            )
        }
        if (showScheduleAlarmsDialog) {
            AlarmSystemSettingsAlert(
                onDismiss = { showScheduleAlarmsDialog = false }
            )
        }
    }
}

private fun Context.isPermissionGranted(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
        this,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

@Composable
private fun StartBeforeEndAlert(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Time constraints") },
        text = {
            Text(
                text = "Start time of the Reminder must be before the end time."
            )
        },
        confirmButton = {
            Button(
                onClick = { onDismiss() },
                content = { Text(text = "Ok") }
            )
        }
    )
}

@Composable
private fun NotificationPermissionSettingsAlert(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Notification permission denied") },
        text = {
            Text(
                text = "The app is unable to show Reminders " +
                        "without the notification permission."
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    context.goToNotificationPermissionSettings()
                    onDismiss()
                },
                content = { Text(text = "Go To Settings") }
            )
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                content = { Text(text = "Cancel") }
            )
        }
    )
}

private fun Context.goToNotificationPermissionSettings() {
    startActivity(
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:$packageName")
        }
    )
}

@Composable
private fun AlarmSystemSettingsAlert(onDismiss: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Alarm permission denied") },
        text = {
            Text(
                text = "The app is unable to show Reminders " +
                        "without the alarm permission."
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    context.goToAlarmSystemSettings()
                    onDismiss()
                },
                content = { Text(text = "Go To Settings") }
            )
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                content = { Text(text = "Cancel") }
            )
        }
    )
}

private fun Context.goToAlarmSystemSettings() {
    startActivity(
        Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
            data = Uri.parse("package:$packageName")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
    )
}


@Composable
fun SettingsSection(
    modifier: Modifier = Modifier,
    title: String? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 20.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (title != null) {
                SettingTitle(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    name = title
                )
            }
            content()
        }
    }
}

@Composable
private fun SettingTitle(
    modifier: Modifier = Modifier,
    name: String,
) {
    Text(
        modifier = modifier,
        text = name,
        color = MaterialTheme.colorScheme.surface,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
fun SettingItem(
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    fieldName: String,
    value: String? = null,
    onClick: () -> Unit,
) {
    HydroListItem(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp),
        headlineContent = {
            Text(
                text = fieldName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.surface
            )
        },
        supportingContent = {
            if (value != null) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        leadingContent = { icon() }
    )
}

@Composable
fun ToggleSettingItem(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    fieldName: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    HydroListItem(
        modifier = modifier.padding(horizontal = 20.dp),
        headlineContent = {
            Text(
                text = fieldName,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.surface
            )
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = "set $fieldName"
            )
        },
        trailingContent = {
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    )
}

@Composable
internal fun HydroListItem(
    modifier: Modifier = Modifier,
    headlineContent: @Composable () -> Unit,
    overlineContent: @Composable (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    supportingContent: @Composable (() -> Unit)? = null,
) {
    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        overlineContent = overlineContent,
        headlineContent = headlineContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        supportingContent = supportingContent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String = "Select Time",
    initialHour: Int = 0,
    initialMinute: Int = 0,
    onConfirm: (hour: Int, minute: Int) -> Unit,
    onCancel: () -> Unit = {},
) {
    val state = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute
    )
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            color = MaterialTheme.colorScheme.background,
            shape = MaterialTheme.shapes.extraLarge,
            tonalElevation = 6.dp,
            modifier = Modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min)
                .background(
                    shape = MaterialTheme.shapes.extraLarge,
                    color = MaterialTheme.colorScheme.background
                )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )

                TimePicker(state = state, colors = TimePickerDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.background,
                    clockDialColor = MaterialTheme.colorScheme.background,
                    clockDialSelectedContentColor = MaterialTheme.colorScheme.surface,
                    clockDialUnselectedContentColor = MaterialTheme.colorScheme.surface,
                    timeSelectorSelectedContainerColor = MaterialTheme.colorScheme.background,
                    timeSelectorUnselectedContentColor = MaterialTheme.colorScheme.surface,
                    timeSelectorSelectedContentColor = MaterialTheme.colorScheme.surface,
                    timeSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background ,
                    periodSelectorSelectedContentColor = MaterialTheme.colorScheme.surface,
                    periodSelectorUnselectedContainerColor = MaterialTheme.colorScheme.background,
                    periodSelectorSelectedContainerColor = MaterialTheme.colorScheme.background,
                    periodSelectorUnselectedContentColor = MaterialTheme.colorScheme.surface,
                    periodSelectorBorderColor = MaterialTheme.colorScheme.surface
                ))
                Row(
                    modifier = Modifier
                        .height(40.dp)
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(
                        onClick = onCancel
                    ) { Text("Cancel") }
                    TextButton(
                        onClick = { onConfirm(state.hour, state.minute) }
                    ) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun CupCarouselSelection(
    modifier: Modifier = Modifier,
    state: AppState,
    dispatch: (AppAction) -> Unit,
) {
    Column(modifier = modifier) {
        var showCanOnlySelectThreeAlert by remember { mutableStateOf(false) }
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "Cups",
            color = MaterialTheme.colorScheme.surface,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            modifier = Modifier.padding(horizontal = 24.dp),
            text = "Cups are displayed on your Main screen and in your Reminder notification",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        val allCupsAsMilliliters = remember(state.allCups) {
            state.allCups.map(Cup::milliliters)
        }
        val selectedCupsAsMilliliters = remember(state.selectedCups) {
            state.selectedCups.map(Cup::milliliters)
        }
        HydrationCarousel(
            contentPadding = PaddingValues(horizontal = 36.dp),
            milliliterItems = allCupsAsMilliliters,
            liquidUnit = state.liquidUnit,
            selected = selectedCupsAsMilliliters,
            onClick = { index, _ ->
                val cup = state.allCups[index]
                if (cup in state.selectedCups) {
                    dispatch(AppAction.SetSelectedCups(state.selectedCups - cup))
                } else if (state.selectedCups.count() >= 3) {
                    showCanOnlySelectThreeAlert = true
                } else {
                    dispatch(AppAction.SetSelectedCups(state.selectedCups + cup))
                }
            }
        )
        if (showCanOnlySelectThreeAlert) {
            AlertDialog(
                title = { Text(text = "Sorry") },
                text = { Text(text = "You can only select 3 different Cups.") },
                onDismissRequest = { showCanOnlySelectThreeAlert = false },
                confirmButton = {
                    Button(
                        onClick = { showCanOnlySelectThreeAlert = false },
                        content = { Text(text = "Ok") }
                    )
                }
            )
        }
    }
}

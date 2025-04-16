package com.companies.smartwaterintake.presentation.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.R
import com.companies.smartwaterintake.data.format
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.minutes

@Composable
fun SetIntervalDialog(
    state: AppState,
    dispatch: (AppAction) -> Unit,
    onClose: () -> Unit
) {
    check(state.reminder != null)
    var value = remember(state.reminder) {
        mutableFloatStateOf(state.reminder.interval.inWholeMinutes.toFloat())
    }
    val intervalValue = remember(value) {
        derivedStateOf { value.floatValue.roundToInt().minutes }
    }

    androidx.compose.material3.AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        title = {
            Text(
                text = "Interval",
                color = MaterialTheme.colorScheme.surface,
                style = MaterialTheme.typography.titleLarge
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    dispatch(
                        AppAction.SetReminder(state.reminder.copy(interval = intervalValue.value))
                    )
                    onClose()
                },
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Save")
            }
        },
        onDismissRequest = onClose,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp), // Add padding to prevent overlap
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(R.drawable.ic_arrow_range),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.surface
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Slider(
                        value = value.floatValue,
                        valueRange = 1f..60f,
                        onValueChange = { minutesValue -> value.floatValue = minutesValue },
                        colors = SliderDefaults.colors(
                            activeTickColor = MaterialTheme.colorScheme.surface,
                            inactiveTickColor = MaterialTheme.colorScheme.surface,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = MaterialTheme.colorScheme.primary,
                            thumbColor = MaterialTheme.colorScheme.surface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = intervalValue.value.format(),
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        titleContentColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.surface

    )
}

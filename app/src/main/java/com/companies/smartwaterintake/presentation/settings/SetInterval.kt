package com.companies.smartwaterintake.presentation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
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
    AlertDialog(
        onDismissRequest = onClose,
        title = {
            Text("Interval")
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_arrow_range),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                Slider(
                    value = value.floatValue,
                    valueRange = 1f..60f,
                    onValueChange = { minutesValue -> value.floatValue = minutesValue }
                )
                Text(text = intervalValue.value.format())
            }


        },
        buttons = {
            TextButton(
                onClick = {
                    dispatch(
                        AppAction.SetReminder(state.reminder.copy(interval = intervalValue.value))
                    )
                    onClose()
                }
            ) { Text("Save") }
        }
    )
}
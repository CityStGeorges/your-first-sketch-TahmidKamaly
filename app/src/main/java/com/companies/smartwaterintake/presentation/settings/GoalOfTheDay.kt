package com.companies.smartwaterintake.presentation.settings

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.data.Milliliters
import com.companies.smartwaterintake.data.format
import kotlin.math.roundToInt

@Composable
fun GoalOfTheDayDialog(
    state: AppState,
    dispatch: (AppAction) -> Unit,
    onClose : () -> Unit
) {
    AlertDialog(
        backgroundColor = MaterialTheme.colorScheme.background,
        title = {
            Text("Goal of the Day")
        },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.WaterDrop,
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(16.dp))
                MillilitersSlider(
                    range = remember { Milliliters.DAILY_GOAL_MIN..Milliliters.DAILY_GOAL_MAX },
                    stepsSize = remember { Milliliters.DAILY_GOAL_STEPS },
                    milliliters = state.dailyGoal,
                    onMillilitersChanged = { dispatch(AppAction.SetDailyGoal(it)) }
                )
            }
            Text(text = state.dailyGoal.format(state.liquidUnit), color = MaterialTheme.colorScheme.surface)
        },
        onDismissRequest = onClose,
        buttons = {}
    )
}

@Composable
private fun MillilitersSlider(
    range: ClosedRange<Milliliters>,
    stepsSize: Milliliters,
    milliliters: Milliliters,
    onMillilitersChanged: (Milliliters) -> Unit
) {
    val valueRange = remember(range) {
        range.start.value.toFloat()..range.endInclusive.value.toFloat()
    }
    val steps = remember(valueRange) {
        val itemCount = (valueRange.endInclusive - valueRange.start).roundToInt()
        if (stepsSize.value == 1) 0 else (itemCount / stepsSize.value - 1)
    }
    Slider(
        value = milliliters.value.toFloat(),
        valueRange = valueRange,
        steps = steps,
        onValueChange = { onMillilitersChanged(Milliliters(it.roundToInt())) },
        colors = SliderDefaults.colors(
            activeTickColor = Color.Transparent,
            inactiveTickColor = Color.Transparent
        )
    )
}

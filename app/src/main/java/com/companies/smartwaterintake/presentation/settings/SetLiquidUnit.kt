package com.companies.smartwaterintake.presentation.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.data.LiquidUnit
import com.companies.smartwaterintake.data.format
import com.companies.smartwaterintake.data.formatMoreInfo

@Composable
fun SetLiquidUnitDialog(
    state: AppState,
    dispatch: (AppAction) -> Unit,
    onClose: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onClose,
        backgroundColor = MaterialTheme.colorScheme.background,
        title = { Text("Measurement Unit") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            ) {
                LiquidUnit.entries.forEach { liquidUnit ->
                    HydroListItem(
                        modifier = Modifier
                            .clickable {
                                dispatch(AppAction.SetLiquidUnit(liquidUnit))
                                onClose()
                            }
                            .padding(vertical = 8.dp), // Space between items
                        headlineContent = { Text(text = liquidUnit.format()) },
                        supportingContent = { Text(text = liquidUnit.formatMoreInfo()) },
                        trailingContent = {
                            if (state.liquidUnit == liquidUnit) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "selected"
                                )
                            }
                        }
                    )
                    Divider() // Divider between units
                }
            }
        },
        buttons = {}
    )
}

package com.companies.smartwaterintake.presentation.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.AlertDialog
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.data.Theme
import com.companies.smartwaterintake.data.format
import com.companies.smartwaterintake.data.icon

@Composable
fun ThemeDialog(
    state: AppState,
    dispatch: (AppAction) -> Unit,
    onClose: () -> Unit,
) {
    AlertDialog(
        title = { Text("Theme") },
        backgroundColor = MaterialTheme.colorScheme.background,
        text = {
            Column {
                Theme.entries.forEach { theme ->
                    Column(
                        modifier = Modifier
                            .clickable {
                                dispatch(AppAction.SetTheme(theme))
                                onClose()
                            }
                            .fillMaxWidth()
                            .padding(16.dp) // add padding inside each clickable area
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = theme.icon(),
                                contentDescription = "theme icon",
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = theme.format(),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            if (state.theme == theme) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = "selected",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (theme == Theme.System) {
                            Text(
                                text = "Current system theme is ${if (isSystemInDarkTheme()) "dark" else "light"}.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(start = 36.dp)
                            )
                        }

                        if (theme == Theme.Dynamic) {
                            Text(
                                text = "Uses your phone's wallpaper to determine colors.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier.padding(start = 36.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Divider()
                    }
                }
            }
        },
        onDismissRequest = onClose,
        buttons = {}
    )
}


package com.companies.smartwaterintake.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.presentation.home.ChangeUsernameDialog
import com.companies.smartwaterintake.presentation.navigation.BottomNavigationBar

@Composable
fun ProfileScreen(
    state: AppState,
    dispatch: (AppAction) -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    navHostController: NavHostController,
) {
    val username = viewModel.username.collectAsState() // Collecting the username
    val name = username.value
    var changeUsername by remember { mutableStateOf(name) } // Separate state for editing
    var changeUsernameDialog by remember { mutableStateOf(false) }
    var height by remember {
        mutableStateOf(state.height)
    }

    var weight by remember {
        mutableStateOf(state.weight)
    }

    var heightDialog by remember {
        mutableStateOf(false)
    }
    var weightDialog by remember {
        mutableStateOf(false)
    }


    LaunchedEffect(changeUsernameDialog) {
        if (changeUsernameDialog) {
            changeUsername = name
        }
    }
    if (changeUsernameDialog) {
        ChangeUsernameDialog(
            username = changeUsername,
            onSave = {
                viewModel.changeUsername(changeUsername) // Save only on button click
                changeUsernameDialog = false
            },
            onDismiss = { changeUsernameDialog = false },
            onUsernameChange = { changeUsername = it } // Update local state
        )
    }
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = { BottomNavigationBar(navController = navHostController) }
    ) {
        if (heightDialog) {
            HeightDialog(
                textValue = height ?: "",
                onValueChange = { height = it },
                onDismiss = { heightDialog = false },
                onSave = { dispatch(AppAction.setHeight(it)) })
        }

        if (weightDialog) {
            WeightDialog(
                textValue = weight ?: "",
                onValueChange = { weight = it },
                onDismiss = { weightDialog = false }, onSave = {
                    dispatch(AppAction.setWeight(it))
                })
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp, start = 8.dp, end = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(
                    modifier = Modifier
                        .size(150.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.onBackground,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = username.value,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                    )
                }
            }
            Spacer(Modifier.height(40.dp))
            Card(
                onClick = {
                    changeUsernameDialog = true
                }, colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text("Change Username", color = MaterialTheme.colorScheme.surface)
                }
            }
            Card(
                onClick = {
                    weightDialog = true
                }, colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Text( weight ?: "Add Your Weight", color = MaterialTheme.colorScheme.surface)
                    }
                }

            Card(
                onClick = {
                    heightDialog = true
                }, colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                        Text( height ?: "Add Your Height", color = MaterialTheme.colorScheme.surface)
                    }
                }

            Card(
                onClick = {
                    viewModel.logout(navigate = { navHostController.navigate(it) })
                }, colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.onSurface,
                    contentColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(start = 8.dp, end = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Logout", color = MaterialTheme.colorScheme.surface)
                }
            }
        }
    }
}

@Composable
fun HeightDialog(
    textValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = { Text("Add Your Height", color = MaterialTheme.colorScheme.surface) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = onValueChange,
                label = {
                    Text("Height")
                },
                placeholder = {
                    Text("Your Height")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.surface,
                    unfocusedTextColor = MaterialTheme.colorScheme.surface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.surface,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(textValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Save")
            }
        }
    )
}

@Composable
fun WeightDialog(
    textValue: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {

    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        onDismissRequest = onDismiss,
        title = { Text("Add Your Weight", color = MaterialTheme.colorScheme.surface) },
        text = {
            OutlinedTextField(
                value = textValue,
                onValueChange = onValueChange,
                label = {
                    Text("Weight")
                },
                placeholder = {
                    Text("Your Weight")
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.background,
                    unfocusedContainerColor = MaterialTheme.colorScheme.background,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.primary,
                    focusedTextColor = MaterialTheme.colorScheme.surface,
                    unfocusedTextColor = MaterialTheme.colorScheme.surface,
                    focusedLabelColor = MaterialTheme.colorScheme.primary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.primary,
                    focusedPlaceholderColor = MaterialTheme.colorScheme.surface,
                    unfocusedPlaceholderColor = MaterialTheme.colorScheme.surface,
                )
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(textValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Save")
            }
        }
    )
}
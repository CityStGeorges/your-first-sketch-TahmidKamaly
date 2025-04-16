package com.companies.smartwaterintake.presentation.home

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.StepsRecord
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.data.format
import com.companies.smartwaterintake.data.icon
import com.companies.smartwaterintake.data.isDarkTheme
import com.companies.smartwaterintake.presentation.navigation.BottomNavigationBar
import com.companies.smartwaterintake.ui.utils.HealthConnectUtils
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    navHostController: NavHostController,
    state: AppState,
    dispatch: (AppAction) -> Unit,
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
    )
    val weatherResponse = homeViewModel.weatherState.collectAsState()
    val steps = homeViewModel.steps.collectAsState()

    val PERMISSIONS = setOf(
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
    )

    val requestPermissions =
        rememberLauncherForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            if (granted.containsAll(PERMISSIONS)) {
                // Permissions successfully granted , continue with reading the data from health connect
                homeViewModel.loadDailySteps(context)
            } else {
                Toast.makeText(context, "Permissions are rejected", Toast.LENGTH_SHORT).show()

            }

        }

    LaunchedEffect(key1 = true) {
        when (HealthConnectUtils.checkForHealthConnectInstalled(context)) {
            HealthConnectClient.SDK_UNAVAILABLE -> {
                Toast.makeText(
                    context,
                    "Health Connect client is not available for this device",
                    Toast.LENGTH_SHORT
                ).show()

            }


            HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED -> {
                Toast.makeText(
                    context,
                    "Health Connect needs to be installed",
                    Toast.LENGTH_SHORT
                ).show()
            }

            HealthConnectClient.SDK_AVAILABLE -> {
                if (HealthConnectUtils.checkPermissions()) {
                    homeViewModel.loadDailySteps(context)
                } else {
                    requestPermissions.launch(HealthConnectUtils.PERMISSIONS)
                }

            }
        }
    }

    LaunchedEffect(locationPermissionState.status) {
        when (locationPermissionState.status) {
            is PermissionStatus.Granted -> {
                Log.d("HomeScreen", "Permission granted, trying to get location.")
                requestFreshLocation(context) { lat, lon ->
                    Log.d("HomeScreen", "Got location: lat=$lat, lon=$lon")
                    homeViewModel.fetchWeather(lat, lon)
                }
            }

            is PermissionStatus.Denied -> {
                locationPermissionState.launchPermissionRequest()
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            homeViewModel.loadDailySteps(context)
            delay(5 * 60 * 1000L) // Every 5 minutes
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
        bottomBar = { BottomNavigationBar(navController = navHostController) },
        topBar = {
            TopAppBar(
                title = {
                    Text("")
                },
                actions = {
                    IconButton(onClick = {
                        navHostController.navigate(com.companies.smartwaterintake.presentation.navigation.Settings)
                    }) {
                        Icon(Icons.Default.Settings, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    actionIconContentColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {
            // Background stays behind
            HomeBackground(
                isSystemInDarkTheme = state.theme.isDarkTheme(),
                hydrationProgress = state.hydrationProgress
            )

            // Temperature (top)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopStart),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val tempInCelsius = weatherResponse.value?.main?.temp?.minus(273.15)
                dispatch(AppAction.SetTemperature(tempInCelsius ?: 0.0))
                val temperatureText = tempInCelsius?.let { String.format("%.1f°C", it) } ?: "--°C"
                Text(
                    "Temperature: $temperatureText",
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
                Spacer(modifier = Modifier.height(5.dp))
                dispatch(AppAction.setStepRecord(steps.value))
                Text(
                    text = "Steps Today: ${steps.value}",
                    color = MaterialTheme.colorScheme.surface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 25.sp
                )
            }



            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    state.hydrationProgress.format(),
                    color = MaterialTheme.colorScheme.surface,
                    style = MaterialTheme.typography.headlineLarge, // looks better centered
                    fontWeight = FontWeight.Bold
                )
            }

            // Cup carousel (bottom)
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .padding(bottom = 80.dp) // adjust if you need more space above nav bar
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically

            ) {
                state.selectedCups.forEach { cup ->
                    FloatingActionButton(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.surface,
                        onClick = { dispatch(AppAction.AddHydration(cup.milliliters)) },
                        content = {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    modifier = Modifier.size(20.dp),
                                    painter = cup.milliliters.icon(),
                                    contentDescription = "add one cup"
                                )
                                Text(
                                    text = cup.milliliters.format(state.liquidUnit),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ChangeUsernameDialog(
    username: String,
    onUsernameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        containerColor = MaterialTheme.colorScheme.background,
        titleContentColor = MaterialTheme.colorScheme.surface,
        textContentColor = MaterialTheme.colorScheme.surface,

        onDismissRequest = { },
        title = { Text("Change Username") },
        text = {
            Column {
                Text("Enter Your new Username.")
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = username,
                    onValueChange = onUsernameChange,
                    label = {
                        Text(
                            "Username",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    placeholder = {
                        Text(
                            "Enter Username",
                            color = MaterialTheme.colorScheme.onBackground,
                            style = MaterialTheme.typography.bodyLarge
                        )
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
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave()
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    contentColor = MaterialTheme.colorScheme.surface,
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                }, colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

@SuppressLint("MissingPermission")
fun requestFreshLocation(context: Context, onLocationReceived: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    fusedLocationClient.getCurrentLocation(
        Priority.PRIORITY_HIGH_ACCURACY,
        null
    ).addOnSuccessListener { location ->
        if (location != null) {
            Log.d(
                "HomeScreen",
                "Fresh location received: ${location.latitude}, ${location.longitude}"
            )
            onLocationReceived(location.latitude, location.longitude)
        } else {
            Log.e("HomeScreen", "Fresh location is null")
        }
    }.addOnFailureListener { exception ->
        Log.e("HomeScreen", "Failed to get fresh location", exception)
    }
}


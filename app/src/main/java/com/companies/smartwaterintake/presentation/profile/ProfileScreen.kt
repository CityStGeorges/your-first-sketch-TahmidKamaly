package com.companies.smartwaterintake.presentation.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.companies.smartwaterintake.presentation.home.ChangeUsernameDialog
import com.companies.smartwaterintake.presentation.navigation.BottomNavigationBar

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    navHostController: NavHostController,
) {
    val username = viewModel.username.collectAsState() // Collecting the username
    val name = username.value
    var changeUsername by remember { mutableStateOf(name) } // Separate state for editing
    var changeUsernameDialog by remember { mutableStateOf(false) }



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
        bottomBar = { BottomNavigationBar(navController = navHostController) }) {
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
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp, end = 8.dp)
            ) {
                TextButton(onClick = {
                    changeUsernameDialog = true
                }, contentPadding = PaddingValues(0.dp)) {
                    Text("Change Username")
                }
            }
        }
    }
}
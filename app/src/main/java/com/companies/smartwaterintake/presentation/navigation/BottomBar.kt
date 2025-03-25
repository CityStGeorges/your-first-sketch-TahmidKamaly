package com.companies.smartwaterintake.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomBar(var title: String, var icon: ImageVector, var route: String) {
    object HomeScreen : BottomBar(title = "Home", icon = Icons.Filled.Home, route = Home)
    object ProfileScreen : BottomBar("Profile", Icons.Default.Person, Profile)
}
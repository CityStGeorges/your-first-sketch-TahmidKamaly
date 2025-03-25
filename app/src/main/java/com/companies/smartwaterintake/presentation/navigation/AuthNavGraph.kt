package com.companies.smartwaterintake.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.companies.smartwaterintake.presentation.login.LoginScreen

fun NavGraphBuilder.authNavGraph(navHostController: NavHostController) {
    navigation(
        route = Graph.Authentication,
        startDestination = Login
    ) {
        composable(Login) {
            LoginScreen(navigate = { route -> navHostController.navigate(route) })
        }
    }
}
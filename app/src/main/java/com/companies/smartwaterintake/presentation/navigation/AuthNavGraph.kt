package com.companies.smartwaterintake.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.companies.smartwaterintake.presentation.login.LoginScreen
import com.companies.smartwaterintake.presentation.register.RegisterScreen
import com.companies.smartwaterintake.presentation.splash.SplashScreen

fun NavGraphBuilder.authNavGraph(navHostController: NavHostController) {
    navigation(
        route = Graph.Authentication,
        startDestination = Splash
    ) {
        composable(Splash) {
            SplashScreen(navigate = { route -> navHostController.navigate(route) })
        }
        composable(Login) {
            LoginScreen(navigate = { route -> navHostController.navigate(route) })
        }

        composable(Register) {
            RegisterScreen(navigate = { route -> navHostController.navigate(route) })
        }
    }
}
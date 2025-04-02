package com.companies.smartwaterintake.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.companies.smartwaterintake.presentation.home.HomeScreen
import com.companies.smartwaterintake.presentation.profile.ProfileScreen

fun NavGraphBuilder.HomeNavGraph(
    navHostController: NavHostController,
) {
    navigation(
        route = Graph.Home,
        startDestination = BottomBar.HomeScreen.route
    ) {
        composable(
            route = BottomBar.HomeScreen.route,
        ) {
            HomeScreen(navHostController = navHostController)
        }

        composable(route = BottomBar.ProfileScreen.route) {
            ProfileScreen(navHostController = navHostController)
        }

    }
}
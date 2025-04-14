package com.companies.smartwaterintake.presentation.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState
import com.companies.smartwaterintake.presentation.home.HomeScreen
import com.companies.smartwaterintake.presentation.profile.ProfileScreen
import com.companies.smartwaterintake.presentation.settings.SettingsScreen

fun NavGraphBuilder.HomeNavGraph(
    navHostController: NavHostController,
    state: AppState,
    dispatch: (AppAction) -> Unit,
) {
    navigation(
        route = Graph.Home,
        startDestination = BottomBar.HomeScreen.route
    ) {
        composable(
            route = BottomBar.HomeScreen.route,
        ) {
            HomeScreen(navHostController = navHostController, state = state, dispatch = dispatch)
        }

        composable(route = BottomBar.ProfileScreen.route) {
            ProfileScreen(navHostController = navHostController)
        }

        composable(Settings) {
            SettingsScreen(
                state = state,
                dispatch = dispatch,
            )
        }

    }
}
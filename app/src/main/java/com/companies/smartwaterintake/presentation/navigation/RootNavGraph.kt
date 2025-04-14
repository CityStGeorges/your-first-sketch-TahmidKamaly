package com.companies.smartwaterintake.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import com.companies.smartwaterintake.AppAction
import com.companies.smartwaterintake.AppState

@Composable
fun RootNavigationGraphBuilder(
    navHostController: NavHostController, state: AppState,
    dispatch: (AppAction) -> Unit,
) {
    NavHost(
        navController = navHostController,
        startDestination = Graph.Authentication,
        route = Graph.Root
    ) {
        authNavGraph(navHostController)
        HomeNavGraph(navHostController = navHostController, state = state, dispatch = dispatch)
    }


}

object Graph {
    const val Root = "root_graph"
    const val Authentication = "auth_graph"
    const val Home = "home_graph"
}
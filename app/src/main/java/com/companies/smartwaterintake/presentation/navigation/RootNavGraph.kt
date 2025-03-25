package com.companies.smartwaterintake.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost

@Composable
fun RootNavigationGraphBuilder(navHostController: NavHostController) {
    NavHost(navController = navHostController, startDestination = Graph.Authentication, route = Graph.Root) {
        authNavGraph(navHostController)
        HomeNavGraph(navHostController = navHostController)
    }


}

object Graph {
    const val Root = "root_graph"
    const val Authentication = "auth_graph"
    const val Home = "home_graph"
}
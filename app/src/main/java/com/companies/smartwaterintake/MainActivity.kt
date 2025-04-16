package com.companies.smartwaterintake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.companies.smartwaterintake.data.Theme
import com.companies.smartwaterintake.data.isDarkTheme
import com.companies.smartwaterintake.presentation.navigation.RootNavigationGraphBuilder
import com.companies.smartwaterintake.ui.theme.SmartWaterIntakeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val store = App.instance.store
        enableEdgeToEdge()
        setContent {
            val state = store.state.collectAsStateWithLifecycle()
            SmartWaterIntakeTheme(
                darkTheme = state.value.theme.isDarkTheme(),
            ) {
                RootNavigationGraphBuilder(
                    navHostController = rememberNavController(),
                    state = state.value,
                    dispatch = store::dispatch
                )
            }
        }
    }
}

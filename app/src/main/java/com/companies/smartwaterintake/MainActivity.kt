package com.companies.smartwaterintake

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.companies.smartwaterintake.presentation.navigation.RootNavigationGraphBuilder
import com.companies.smartwaterintake.ui.theme.SmartWaterIntakeTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SmartWaterIntakeTheme {
                RootNavigationGraphBuilder(navHostController = rememberNavController())

            }
        }
    }
}

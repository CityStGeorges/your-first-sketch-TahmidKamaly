package com.companies.smartwaterintake.data

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ColorLens
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class Theme(
    val serialized: String
) {
    System("system"),
    Dark("dark"),
    Light("light");

    companion object {
        fun of(serialized: String?) = if (serialized == null) {
            System
        } else {
            entries.firstOrNull { it.serialized == serialized } ?: System
        }
    }
}

@Composable
fun Theme.isDarkTheme(): Boolean {
    return when (this) {
        Theme.Dark -> true
        Theme.Light -> false
        Theme.System -> isSystemInDarkTheme()
    }
}

fun Theme.format(): String {
    return when (this) {
        Theme.Dark -> "Dark"
        Theme.Light -> "Light"
        Theme.System -> "System"
    }
}

@Composable
fun Theme.icon(): ImageVector {
    return when (this) {
        Theme.Dark -> Icons.Outlined.DarkMode
        Theme.Light -> Icons.Outlined.LightMode
        Theme.System -> Icons.Outlined.InvertColors
    }
}
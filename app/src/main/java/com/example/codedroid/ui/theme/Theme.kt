package com.example.codedroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary        = Color(0xFF64B5F6),
    secondary      = Color(0xFF81C784),
    background     = Color(0xFF0D1117),
    surface        = Color(0xFF161B22),
    surfaceVariant = Color(0xFF21262D),
    onBackground   = Color(0xFFE6EDF3),
    onSurface      = Color(0xFFE6EDF3),
    error          = Color(0xFFFF7B72)
)

private val LightColors = lightColorScheme(
    primary        = Color(0xFF1565C0),
    secondary      = Color(0xFF2E7D32),
    background     = Color(0xFFF0F2F5),
    surface        = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFFE8EAED),
    onBackground   = Color(0xFF1C1C1E),
    onSurface      = Color(0xFF1C1C1E),
    error          = Color(0xFFD32F2F)
)

@Composable
fun CodeDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}
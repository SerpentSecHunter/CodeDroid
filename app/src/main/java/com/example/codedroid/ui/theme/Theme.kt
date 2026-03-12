package com.example.codedroid.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary         = Color(0xFF007ACC),
    secondary       = Color(0xFF4EC9B0),
    tertiary        = Color(0xFFDCDCAA),
    background      = Color(0xFF1E1E1E),
    surface         = Color(0xFF252526),
    surfaceVariant  = Color(0xFF2D2D2D),
    surfaceContainer= Color(0xFF333333),
    onBackground    = Color(0xFFD4D4D4),
    onSurface       = Color(0xFFD4D4D4),
    onSurfaceVariant= Color(0xFF858585),
    outline         = Color(0xFF3C3C3C),
    error           = Color(0xFFF44747)
)

private val LightColors = lightColorScheme(
    primary         = Color(0xFF005F9E),
    secondary       = Color(0xFF267E6E),
    tertiary        = Color(0xFF7C6A00),
    background      = Color(0xFFF3F3F3),
    surface         = Color(0xFFFFFFFF),
    surfaceVariant  = Color(0xFFECECEC),
    surfaceContainer= Color(0xFFE8E8E8),
    onBackground    = Color(0xFF1E1E1E),
    onSurface       = Color(0xFF1E1E1E),
    onSurfaceVariant= Color(0xFF616161),
    outline         = Color(0xFFD4D4D4),
    error           = Color(0xFFCC3333)
)

@Composable
fun CodeDroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content  : @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content     = content
    )
}
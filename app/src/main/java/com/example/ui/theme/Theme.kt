package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElixirBright,
    onPrimary = Color.White,
    primaryContainer = Color(0xFF4C0519),
    onPrimaryContainer = Color(0xFFFFD1DC),
    secondary = GoldAccent,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF451A03),
    onSecondaryContainer = Color(0xFFFEF3C7),
    tertiary = PositiveGreen,
    onTertiary = Color.Black,
    background = EsportsSlate,
    onBackground = DarkTextPrimary,
    surface = EsportsSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = EsportsCard,
    onSurfaceVariant = DarkTextSecondary,
    surfaceContainer = EsportsSurface,
    surfaceContainerHigh = EsportsCard,
    surfaceContainerLow = Color(0xFF080C14),
    outline = EsportsBorder,
    error = DeficitRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

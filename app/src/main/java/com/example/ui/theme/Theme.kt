package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CineBoxDarkColorScheme = darkColorScheme(
    primary = CineRed,
    onPrimary = Color.White,
    primaryContainer = CineRedDark,
    onPrimaryContainer = Color.White,
    secondary = CineGold,
    onSecondary = CineBlack,
    secondaryContainer = CineSurfaceElevated,
    onSecondaryContainer = CineTextPrimary,
    tertiary = CineCyan,
    onTertiary = CineBlack,
    background = CineBlack,
    onBackground = CineTextPrimary,
    surface = CineCharcoal,
    onSurface = CineTextPrimary,
    surfaceVariant = CineSurface,
    onSurfaceVariant = CineTextSecondary,
    surfaceContainerHighest = CineSurfaceElevated,
    outline = CineBorder,
    outlineVariant = CineBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    // CineBox enforces its signature dark cinema theme by default
    MaterialTheme(
        colorScheme = CineBoxDarkColorScheme,
        typography = Typography,
        content = content
    )
}

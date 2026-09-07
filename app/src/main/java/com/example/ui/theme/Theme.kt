package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val HorrorColorScheme = darkColorScheme(
    primary = HorrorColdTeal,
    onPrimary = Color.Black,
    secondary = HorrorAmber,
    onSecondary = Color.Black,
    tertiary = HorrorBloodRed,
    onTertiary = Color.White,
    background = HorrorDarkBackground,
    onBackground = HorrorColdWhite,
    surface = HorrorDarkSurface,
    onSurface = HorrorColdWhite,
    surfaceVariant = HorrorSurfaceVariant,
    onSurfaceVariant = HorrorTextMuted,
    outline = HorrorBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HorrorColorScheme,
        typography = Typography,
        content = content
    )
}

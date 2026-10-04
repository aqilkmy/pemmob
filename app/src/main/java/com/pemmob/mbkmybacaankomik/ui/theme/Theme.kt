package com.pemmob.mbkmybacaankomik.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light Scheme (Default)
private val MbkLightColorScheme = lightColorScheme(
    primary          = MbkPrimary,
    onPrimary        = MbkOnPrimary,
    primaryContainer = MbkSurfaceVariant,
    secondary        = MbkSecondary,
    tertiary         = MbkAccent,
    background       = MbkBackground,
    surface          = MbkSurface,
    surfaceVariant   = MbkSurfaceVariant,
    onBackground     = MbkTextPrimary,
    onSurface        = MbkTextPrimary,
    onSurfaceVariant = MbkTextSecondary,
    outline          = MbkBorder
)

// Dark Scheme
private val MbkDarkColorScheme = darkColorScheme(
    primary          = MbkPrimaryLight,
    onPrimary        = MbkOnPrimary,
    primaryContainer = MbkPrimaryDark,
    secondary        = MbkSecondary,
    tertiary         = MbkAccent,
    background       = MbkDarkBackground,
    surface          = MbkDarkSurface,
    surfaceVariant   = MbkDarkSurfaceVariant,
    onBackground     = MbkDarkTextPrimary,
    onSurface        = MbkDarkTextPrimary,
    onSurfaceVariant = MbkDarkTextSecondary,
    outline          = Color(0xFF2D3748)
)

@Composable
fun MBKMyBacaanKomikTheme(
    // Default sekarang adalah Light Mode (darkTheme = false)
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MbkDarkColorScheme else MbkLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
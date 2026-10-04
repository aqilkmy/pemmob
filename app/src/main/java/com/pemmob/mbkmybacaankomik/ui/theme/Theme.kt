package com.pemmob.mbkmybacaankomik.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Google Material Design 3 Light Color Scheme (Default)
private val MbkLightColorScheme = lightColorScheme(
    primary              = Color(0xFF006C4C),
    onPrimary            = Color(0xFFFFFFFF),
    primaryContainer     = Color(0xFF8AF8C4),
    onPrimaryContainer   = Color(0xFF002114),
    secondary            = Color(0xFF4D6357),
    onSecondary          = Color(0xFFFFFFFF),
    secondaryContainer   = Color(0xFFCFE9D8),
    onSecondaryContainer = Color(0xFF0A1F16),
    tertiary             = Color(0xFF3D6373),
    onTertiary           = Color(0xFFFFFFFF),
    tertiaryContainer    = Color(0xFFC1E8FB),
    onTertiaryContainer  = Color(0xFF001F29),
    error                = Color(0xFFBA1A1A),
    onError              = Color(0xFFFFFFFF),
    errorContainer       = Color(0xFFFFDAD6),
    onErrorContainer     = Color(0xFF410002),
    background           = Color(0xFFF7FBF8),
    onBackground         = Color(0xFF191C1A),
    surface              = Color(0xFFFFFFFF),
    onSurface            = Color(0xFF191C1A),
    surfaceVariant       = Color(0xFFE0EAE2),
    onSurfaceVariant     = Color(0xFF404944),
    outline              = Color(0xFF707973),
    outlineVariant       = Color(0xFFD4DDD6),
    surfaceContainer     = Color(0xFFF0F5F1),
    surfaceContainerHigh = Color(0xFFE8EFEA)
)

// Google Material Design 3 Dark Color Scheme
private val MbkDarkColorScheme = darkColorScheme(
    primary              = Color(0xFF6CDBA9),
    onPrimary            = Color(0xFF003825),
    primaryContainer     = Color(0xFF005238),
    onPrimaryContainer   = Color(0xFF8AF8C4),
    secondary            = Color(0xFFB4CCBD),
    onSecondary          = Color(0xFF20352A),
    secondaryContainer   = Color(0xFF364B40),
    onSecondaryContainer = Color(0xFFCFE9D8),
    tertiary             = Color(0xFFA5CCE0),
    onTertiary           = Color(0xFF073544),
    tertiaryContainer    = Color(0xFF244C5B),
    onTertiaryContainer  = Color(0xFFC1E8FB),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),
    background           = Color(0xFF111413),
    onBackground         = Color(0xFFE1E3DF),
    surface              = Color(0xFF191C1B),
    onSurface            = Color(0xFFE1E3DF),
    surfaceVariant       = Color(0xFF404944),
    onSurfaceVariant     = Color(0xFFC0C9C2),
    outline              = Color(0xFF8A938C),
    outlineVariant       = Color(0xFF404944)
)

@Composable
fun MBKMyBacaanKomikTheme(
    // Default: Light Mode (darkTheme = false)
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
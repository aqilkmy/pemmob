package com.pemmob.mbkmybacaankomik.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MbkDarkColorScheme = darkColorScheme(
    primary          = MbkPrimary,
    onPrimary        = MbkOnPrimary,
    primaryContainer = MbkPrimaryDark,
    secondary        = MbkSecondary,
    tertiary         = MbkAccent,
    background       = MbkBgDark,
    surface          = MbkSurface,
    surfaceVariant   = MbkSurfaceVariant,
    onBackground     = MbkTextPrimary,
    onSurface        = MbkTextPrimary,
    onSurfaceVariant = MbkTextSecondary,
)

// Light scheme (jika diperlukan di masa mendatang)
private val MbkLightColorScheme = lightColorScheme(
    primary          = MbkPrimaryDark,
    onPrimary        = MbkOnPrimary,
    secondary        = MbkSecondary,
    tertiary         = MbkAccent,
    background       = Color(0xFFF5F7FA),
    surface          = Color(0xFFFFFFFF),
    onBackground     = Color(0xFF1A1A2E),
    onSurface        = Color(0xFF1A1A2E)
)

@Composable
fun MBKMyBacaanKomikTheme(
    // App ini menggunakan dark theme secara permanen sesuai desain.
    // Ubah ke false jika ingin mendukung light mode.
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) MbkDarkColorScheme else MbkLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = Typography,
        content     = content
    )
}
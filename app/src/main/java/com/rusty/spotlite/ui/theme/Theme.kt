package com.rusty.spotlite.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val SpotliteGreen = Color(0xFF1ED760)
private val SurfaceDark = Color(0xFF121212)
private val SurfaceVariantDark = Color(0xFF1E1E1E)

private val DarkColors = darkColorScheme(
    primary = SpotliteGreen,
    onPrimary = Color.Black,
    background = SurfaceDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColors = lightColorScheme(
    primary = SpotliteGreen,
    onPrimary = Color.Black,
)

@Composable
fun SpotliteTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    // No dynamic color, no animated theme transitions — one static scheme keeps
    // recomposition cheap, which matters most on the low-end devices this targets.
    val colors = if (darkTheme) DarkColors else LightColors
    MaterialTheme(colorScheme = colors, content = content)
}

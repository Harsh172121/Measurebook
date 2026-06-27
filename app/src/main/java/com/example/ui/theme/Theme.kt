package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RedPrimary,
    secondary = RedSecondary,
    tertiary = RedTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurface, // use Warm Plum Slate as surface variant (cards)
    outline = DarkSurfaceVariant, // use Velvet Border as outline
    outlineVariant = DarkSurfaceVariant, // use Velvet Border as outline
    onPrimary = Color.White,
    onSecondary = Color(0xFF110B0D), // dark charcoal/plum text on golden secondary
    onTertiary = Color.White,
    onBackground = OnDarkSurface,
    onSurface = OnDarkSurface,
    onSurfaceVariant = Color(0xFFC0A2A9) // Muted Rose-Silver for subtle supporting texts
)

private val LightColorScheme = lightColorScheme(
    primary = RedPrimary,
    secondary = RedSecondary,
    tertiary = RedTertiary,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF110B0D),
    onSurface = Color(0xFF110B0D),
    onSurfaceVariant = Color(0xFF705058)
)

@Composable
fun MyApplicationTheme(
    theme: String = "system",
    content: @Composable () -> Unit,
) {
    val darkTheme = when (theme) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ElegantDarkColorScheme = darkColorScheme(
    primary = MawaPrimary,
    onPrimary = MawaOnPrimary,
    primaryContainer = MawaPrimaryContainer,
    onPrimaryContainer = MawaOnPrimaryContainer,
    secondary = MawaSecondary,
    onSecondary = MawaOnSecondary,
    secondaryContainer = MawaSecondaryContainer,
    onSecondaryContainer = MawaOnSecondaryContainer,
    background = MawaBackground,
    onBackground = MawaOnBackground,
    surface = MawaSurface,
    onSurface = MawaOnSurface,
    surfaceVariant = MawaSurfaceVariant,
    onSurfaceVariant = MawaOnSurfaceVariant,
    outline = MawaOutline,
    outlineVariant = MawaOutlineVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = ElegantDarkColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = false
                insetsController.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

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
import com.example.mawa.data.model.AppThemeMode

private val ElegantDarkColorScheme = darkColorScheme(
    primary = MawaPrimaryDark,
    onPrimary = MawaOnPrimaryDark,
    primaryContainer = MawaPrimaryContainerDark,
    onPrimaryContainer = MawaOnPrimaryContainerDark,
    secondary = MawaSecondaryDark,
    onSecondary = MawaOnSecondaryDark,
    secondaryContainer = MawaSecondaryContainerDark,
    onSecondaryContainer = MawaOnSecondaryContainerDark,
    background = MawaBackgroundDark,
    onBackground = MawaOnBackgroundDark,
    surface = MawaSurfaceDark,
    onSurface = MawaOnSurfaceDark,
    surfaceVariant = MawaSurfaceVariantDark,
    onSurfaceVariant = MawaOnSurfaceVariantDark,
    outline = MawaOutlineDark,
    outlineVariant = MawaOutlineVariantDark
)

private val ElegantLightColorScheme = lightColorScheme(
    primary = MawaPrimaryLight,
    onPrimary = MawaOnPrimaryLight,
    primaryContainer = MawaPrimaryContainerLight,
    onPrimaryContainer = MawaOnPrimaryContainerLight,
    secondary = MawaSecondaryLight,
    onSecondary = MawaOnSecondaryLight,
    secondaryContainer = MawaSecondaryContainerLight,
    onSecondaryContainer = MawaOnSecondaryContainerLight,
    background = MawaBackgroundLight,
    onBackground = MawaOnBackgroundLight,
    surface = MawaSurfaceLight,
    onSurface = MawaOnSurfaceLight,
    surfaceVariant = MawaSurfaceVariantLight,
    onSurfaceVariant = MawaOnSurfaceVariantLight,
    outline = MawaOutlineLight,
    outlineVariant = MawaOutlineVariantLight
)

@Composable
fun MyApplicationTheme(
    themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    darkTheme: Boolean = when (themeMode) {
        AppThemeMode.SYSTEM -> isSystemInDarkTheme()
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
    },
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) ElegantDarkColorScheme else ElegantLightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val insetsController = WindowCompat.getInsetsController(window, view)
                insetsController.isAppearanceLightStatusBars = !darkTheme
                insetsController.isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}


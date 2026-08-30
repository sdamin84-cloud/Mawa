package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Elegant Dark Palette (Design System)
val MawaPrimaryDark = Color(0xFFD0BCFF)
val MawaOnPrimaryDark = Color(0xFF381E72)
val MawaPrimaryContainerDark = Color(0xFF4F378B)
val MawaOnPrimaryContainerDark = Color(0xFFEADDFF)
val MawaPillDark = Color(0xFF21005D)

val MawaSecondaryDark = Color(0xFFCCC2DC)
val MawaOnSecondaryDark = Color(0xFF332D41)
val MawaSecondaryContainerDark = Color(0xFF4A4458)
val MawaOnSecondaryContainerDark = Color(0xFFE8DEF8)

val MawaBackgroundDark = Color(0xFF141218)
val MawaOnBackgroundDark = Color(0xFFE6E1E5)
val MawaSurfaceDark = Color(0xFF211F26)
val MawaOnSurfaceDark = Color(0xFFE6E1E5)
val MawaSurfaceVariantDark = Color(0xFF49454F)
val MawaOnSurfaceVariantDark = Color(0xFFCAC4D0)

val MawaOutlineDark = Color(0xFF938F99)
val MawaOutlineVariantDark = Color(0xFF49454F)

// Elegant Light Palette
val MawaPrimaryLight = Color(0xFF6750A4)
val MawaOnPrimaryLight = Color(0xFFFFFFFF)
val MawaPrimaryContainerLight = Color(0xFFEADDFF)
val MawaOnPrimaryContainerLight = Color(0xFF21005D)

val MawaSecondaryLight = Color(0xFF625B71)
val MawaOnSecondaryLight = Color(0xFFFFFFFF)
val MawaSecondaryContainerLight = Color(0xFFE8DEF8)
val MawaOnSecondaryContainerLight = Color(0xFF1D192B)

val MawaBackgroundLight = Color(0xFFF8F9FE)
val MawaOnBackgroundLight = Color(0xFF1C1B1F)
val MawaSurfaceLight = Color(0xFFFFFFFF)
val MawaOnSurfaceLight = Color(0xFF1C1B1F)
val MawaSurfaceVariantLight = Color(0xFFF0ECF4)
val MawaOnSurfaceVariantLight = Color(0xFF49454F)

val MawaOutlineLight = Color(0xFF79747E)
val MawaOutlineVariantLight = Color(0xFFCAC4D0)

// Aliases for compatibility
val MawaPrimary = MawaPrimaryDark
val MawaOnPrimary = MawaOnPrimaryDark
val MawaPrimaryContainer = MawaPrimaryContainerDark
val MawaOnPrimaryContainer = MawaOnPrimaryContainerDark

val MawaSecondary = MawaSecondaryDark
val MawaOnSecondary = MawaOnSecondaryDark
val MawaSecondaryContainer = MawaSecondaryContainerDark
val MawaOnSecondaryContainer = MawaOnSecondaryContainerDark

val MawaBackground = MawaBackgroundDark
val MawaOnBackground = MawaOnBackgroundDark
val MawaSurface = MawaSurfaceDark
val MawaOnSurface = MawaOnSurfaceDark
val MawaSurfaceVariant = MawaSurfaceVariantDark
val MawaOnSurfaceVariant = MawaOnSurfaceVariantDark

val MawaOutline = MawaOutlineDark
val MawaOutlineVariant = MawaOutlineVariantDark

// Financial Semantic Tokens for Dark Mode
val FinancialPositiveDark = Color(0xFF81C784)
val FinancialPositiveContainerDark = Color(0xFF1E3A2F)
val FinancialPositiveOnContainerDark = Color(0xFFA8E6CF)

val FinancialNegativeDark = Color(0xFFFFB4AB)
val FinancialNegativeContainerDark = Color(0xFF5C1D1D)
val FinancialNegativeOnContainerDark = Color(0xFFFFDAD6)

val FinancialWarningDark = Color(0xFFFFD8E4)
val FinancialWarningContainerDark = Color(0xFF633B48)
val FinancialWarningOnContainerDark = Color(0xFFFFD8E4)

val FinancialNeutralDark = Color(0xFFCAC4D0)

// Financial Semantic Tokens for Light Mode (High Contrast & Crisp)
val FinancialPositiveLight = Color(0xFF1B5E20)
val FinancialPositiveContainerLight = Color(0xFFE8F5E9)
val FinancialPositiveOnContainerLight = Color(0xFF00210B)

val FinancialNegativeLight = Color(0xFFBA1A1A)
val FinancialNegativeContainerLight = Color(0xFFFFDAD6)
val FinancialNegativeOnContainerLight = Color(0xFF410002)

val FinancialWarningLight = Color(0xFFC2185B)
val FinancialWarningContainerLight = Color(0xFFFCE4EC)
val FinancialWarningOnContainerLight = Color(0xFF3E001D)

val FinancialNeutralLight = Color(0xFF49454F)

// Data holder for dynamic theme-aware financial colors
data class FinancialColors(
    val positive: Color,
    val positiveContainer: Color,
    val positiveOnContainer: Color,
    val negative: Color,
    val negativeContainer: Color,
    val negativeOnContainer: Color,
    val warning: Color,
    val warningContainer: Color,
    val warningOnContainer: Color,
    val neutral: Color
)

val DarkFinancialColors = FinancialColors(
    positive = FinancialPositiveDark,
    positiveContainer = FinancialPositiveContainerDark,
    positiveOnContainer = FinancialPositiveOnContainerDark,
    negative = FinancialNegativeDark,
    negativeContainer = FinancialNegativeContainerDark,
    negativeOnContainer = FinancialNegativeOnContainerDark,
    warning = FinancialWarningDark,
    warningContainer = FinancialWarningContainerDark,
    warningOnContainer = FinancialWarningOnContainerDark,
    neutral = FinancialNeutralDark
)

val LightFinancialColors = FinancialColors(
    positive = FinancialPositiveLight,
    positiveContainer = FinancialPositiveContainerLight,
    positiveOnContainer = FinancialPositiveOnContainerLight,
    negative = FinancialNegativeLight,
    negativeContainer = FinancialNegativeContainerLight,
    negativeOnContainer = FinancialNegativeOnContainerLight,
    warning = FinancialWarningLight,
    warningContainer = FinancialWarningContainerLight,
    warningOnContainer = FinancialWarningOnContainerLight,
    neutral = FinancialNeutralLight
)

val LocalFinancialColors = androidx.compose.runtime.staticCompositionLocalOf { LightFinancialColors }

// Dynamic accessors that adapt automatically to Light or Dark mode
val FinancialPositive: Color
    @Composable get() = LocalFinancialColors.current.positive

val FinancialPositiveContainer: Color
    @Composable get() = LocalFinancialColors.current.positiveContainer

val FinancialPositiveOnContainer: Color
    @Composable get() = LocalFinancialColors.current.positiveOnContainer

val FinancialNegative: Color
    @Composable get() = LocalFinancialColors.current.negative

val FinancialNegativeContainer: Color
    @Composable get() = LocalFinancialColors.current.negativeContainer

val FinancialNegativeOnContainer: Color
    @Composable get() = LocalFinancialColors.current.negativeOnContainer

val FinancialWarning: Color
    @Composable get() = LocalFinancialColors.current.warning

val FinancialWarningContainer: Color
    @Composable get() = LocalFinancialColors.current.warningContainer

val FinancialWarningOnContainer: Color
    @Composable get() = LocalFinancialColors.current.warningOnContainer

val FinancialNeutral: Color
    @Composable get() = LocalFinancialColors.current.neutral



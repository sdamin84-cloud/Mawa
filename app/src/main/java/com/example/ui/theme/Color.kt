package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Soothing, Eye-Friendly Modern Financial Palette (Emerald & Midnight Slate)
// Designed for anti-glare comfort, harmonious contrast, and consistent retail identity.

// Dark Palette (Restful, Deep Midnight Slate, No blinding neon)
val MawaPrimaryDark = Color(0xFF34D399) // Soothing Mint / Emerald 400
val MawaOnPrimaryDark = Color(0xFF064E3B)
val MawaPrimaryContainerDark = Color(0xFF064E3B)
val MawaOnPrimaryContainerDark = Color(0xFFA7F3D0)
val MawaPillDark = Color(0xFF0F392B)

val MawaSecondaryDark = Color(0xFF2DD4BF) // Gentle Teal 400
val MawaOnSecondaryDark = Color(0xFF134E4A)
val MawaSecondaryContainerDark = Color(0xFF134E4A)
val MawaOnSecondaryContainerDark = Color(0xFFCCFBF1)

val MawaBackgroundDark = Color(0xFF0F172A) // Slate 900 (Soft, comfortable dark background)
val MawaOnBackgroundDark = Color(0xFFF8FAFC)
val MawaSurfaceDark = Color(0xFF1E293B) // Slate 800 (Refined card surface)
val MawaOnSurfaceDark = Color(0xFFF8FAFC)
val MawaSurfaceVariantDark = Color(0xFF334155) // Slate 700
val MawaOnSurfaceVariantDark = Color(0xFF94A3B8) // Slate 400

val MawaOutlineDark = Color(0xFF475569) // Slate 600
val MawaOutlineVariantDark = Color(0xFF334155) // Slate 700

// Light Palette (Calming, Clean, Anti-glare Paper Warmth)
val MawaPrimaryLight = Color(0xFF047857) // Deep Emerald 700 (Prestigious, Trustworthy)
val MawaOnPrimaryLight = Color(0xFFFFFFFF)
val MawaPrimaryContainerLight = Color(0xFFECFDF5) // Very Soft Mint (Comfortable for long eyes usage)
val MawaOnPrimaryContainerLight = Color(0xFF064E3B)

val MawaSecondaryLight = Color(0xFF0F766E) // Deep Teal 700
val MawaOnSecondaryLight = Color(0xFFFFFFFF)
val MawaSecondaryContainerLight = Color(0xFFCCFBF1)
val MawaOnSecondaryContainerLight = Color(0xFF115E59)

val MawaBackgroundLight = Color(0xFFF8FAFC) // Soft Paper Slate 50 (Prevents eye fatigue)
val MawaOnBackgroundLight = Color(0xFF0F172A) // Crisp Slate 900
val MawaSurfaceLight = Color(0xFFFFFFFF)
val MawaOnSurfaceLight = Color(0xFF0F172A)
val MawaSurfaceVariantLight = Color(0xFFF1F5F9) // Slate 100
val MawaOnSurfaceVariantLight = Color(0xFF64748B) // Slate 500

val MawaOutlineLight = Color(0xFFCBD5E1) // Slate 300
val MawaOutlineVariantLight = Color(0xFFE2E8F0) // Slate 200

// Aliases for compatibility
val MawaPrimary = MawaPrimaryLight
val MawaOnPrimary = MawaOnPrimaryLight
val MawaPrimaryContainer = MawaPrimaryContainerLight
val MawaOnPrimaryContainer = MawaOnPrimaryContainerLight

val MawaSecondary = MawaSecondaryLight
val MawaOnSecondary = MawaOnSecondaryLight
val MawaSecondaryContainer = MawaSecondaryContainerLight
val MawaOnSecondaryContainer = MawaOnSecondaryContainerLight

val MawaBackground = MawaBackgroundLight
val MawaOnBackground = MawaOnBackgroundLight
val MawaSurface = MawaSurfaceLight
val MawaOnSurface = MawaOnSurfaceLight
val MawaSurfaceVariant = MawaSurfaceVariantLight
val MawaOnSurfaceVariant = MawaOnSurfaceVariantLight

val MawaOutline = MawaOutlineLight
val MawaOutlineVariant = MawaOutlineVariantLight

// Financial Semantic Tokens for Dark Mode (Restful, non-blinding)
val FinancialPositiveDark = Color(0xFF34D399) // Mint / Emerald 400
val FinancialPositiveContainerDark = Color(0xFF064E3B)
val FinancialPositiveOnContainerDark = Color(0xFFA7F3D0)

val FinancialNegativeDark = Color(0xFFFB7185) // Rose 400 (Soft, comfortable)
val FinancialNegativeContainerDark = Color(0xFF4C0519)
val FinancialNegativeOnContainerDark = Color(0xFFFFE4E6)

val FinancialWarningDark = Color(0xFFFBBF24) // Amber 400 (Warm, clear)
val FinancialWarningContainerDark = Color(0xFF451A03)
val FinancialWarningOnContainerDark = Color(0xFFFEF3C7)

val FinancialNeutralDark = Color(0xFF94A3B8)

// Financial Semantic Tokens for Light Mode (Crisp, gentle, harmonious)
val FinancialPositiveLight = Color(0xFF059669) // Emerald 600
val FinancialPositiveContainerLight = Color(0xFFECFDF5)
val FinancialPositiveOnContainerLight = Color(0xFF064E3B)

val FinancialNegativeLight = Color(0xFFE11D48) // Rose 600 (Eye-friendly, not neon red)
val FinancialNegativeContainerLight = Color(0xFFFFF1F2)
val FinancialNegativeOnContainerLight = Color(0xFF881337)

val FinancialWarningLight = Color(0xFFD97706) // Amber 600
val FinancialWarningContainerLight = Color(0xFFFEF3C7)
val FinancialWarningOnContainerLight = Color(0xFF78350F)

val FinancialNeutralLight = Color(0xFF64748B)

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



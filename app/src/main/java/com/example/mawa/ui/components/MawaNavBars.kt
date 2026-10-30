package com.example.mawa.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material.icons.outlined.Store
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.model.AppMode
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.LocalAppLanguage
import com.example.ui.theme.MawaPrimary

/**
 * Helper to trigger a crisp mechanical click beat (haptic pulse)
 */
fun triggerClickBeat(context: Context, haptic: androidx.compose.ui.hapticfeedback.HapticFeedback) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)?.vibrate(18)
        }
    } catch (_: Throwable) {
        try {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        } catch (_: Throwable) {}
    }
}

enum class MawaTab(
    val titleBangla: String,
    val titleEnglish: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("হোম", "Home", Icons.Filled.Store, Icons.Outlined.Store, "tab_home"),
    FORDI("ফর্দ", "Fordi", Icons.Filled.Assignment, Icons.Outlined.Assignment, "tab_fordi"),
    BAKI("বাকি", "Baki", Icons.Filled.People, Icons.Outlined.People, "tab_baki"),
    EXPENSE("খরচ", "Expense", Icons.Filled.Receipt, Icons.Outlined.Receipt, "tab_expense"),
    PERSONAL("ব্যক্তিগত", "Personal", Icons.Filled.AccountBalanceWallet, Icons.Outlined.AccountBalanceWallet, "tab_personal"),
    HOME_ACCOUNTING("বাড়ির হিসাব", "Household", Icons.Filled.Home, Icons.Outlined.Home, "tab_home_accounting"),
    REPORTS("রিপোর্ট", "Reports", Icons.Filled.Assessment, Icons.Outlined.Assessment, "tab_reports");

    fun getTitle(isEnglish: Boolean): String = if (isEnglish) titleEnglish else titleBangla
}

@Composable
fun MawaBottomBar(
    currentTab: MawaTab,
    onTabSelected: (MawaTab) -> Unit,
    onQuickExpenseClick: () -> Unit,
    modifier: Modifier = Modifier,
    appMode: AppMode = AppMode.BOTH
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    // Exactly matching screenshot: 3 on left, center (+) খরচ, 3 on right
    val leftTabs = listOf(MawaTab.HOME, MawaTab.FORDI, MawaTab.BAKI)
    val rightTabs = listOf(MawaTab.PERSONAL, MawaTab.HOME_ACCOUNTING, MawaTab.REPORTS)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars),
        contentAlignment = Alignment.BottomCenter
    ) {
        // Main Navigation Bar Surface with clean, modern elevated rounded edge
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 8.dp,
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                ),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            tonalElevation = 2.dp
        ) {
            Column {
                // Subtle top border divider
                HorizontalDivider(
                    thickness = 0.8.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                        .padding(horizontal = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left 3 Tabs: হোম, ফর্দ, বাকি
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        leftTabs.forEach { tab ->
                            MawaTabItem(
                                tab = tab,
                                isSelected = currentTab == tab,
                                onClick = {
                                    triggerClickBeat(context, haptic)
                                    onTabSelected(tab)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Clear Center Cradle Spacer for Floating Action Button
                    Spacer(modifier = Modifier.width(54.dp))

                    // Right 3 Tabs: ব্যক্তিগত, বাড়ির হিসাব, রিপোর্ট
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rightTabs.forEach { tab ->
                            MawaTabItem(
                                tab = tab,
                                isSelected = currentTab == tab,
                                onClick = {
                                    triggerClickBeat(context, haptic)
                                    onTabSelected(tab)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // Center Elevated Floating Action Button with [খরচ] label directly underneath it
        val centerInteractionSource = remember { MutableInteractionSource() }
        val isCenterPressed by centerInteractionSource.collectIsPressedAsState()

        val centerScale by animateFloatAsState(
            targetValue = if (isCenterPressed) 0.90f else 1.0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "center_fab_scale"
        )

        val centerRotation by animateFloatAsState(
            targetValue = if (isCenterPressed) 45f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "center_fab_rot"
        )

        val primaryColor = MaterialTheme.colorScheme.primary

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-18).dp)
                .scale(centerScale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = centerInteractionSource,
                        indication = null,
                        onClick = {
                            triggerClickBeat(context, haptic)
                            onQuickExpenseClick()
                        }
                    )
                    .testTag("btn_center_quick_expense"),
                shape = CircleShape,
                color = primaryColor,
                shadowElevation = 8.dp,
                border = BorderStroke(2.5.dp, MaterialTheme.colorScheme.surface)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "খরচ যোগ করুন",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .size(26.dp)
                            .rotate(centerRotation)
                    )
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            val isEnglish = LocalAppLanguage.current.isEnglish
            Text(
                text = if (isEnglish) "Expense" else "খরচ",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                fontSize = 11.sp
            )
        }
    }
}

fun getMawaTabThemeColor(tab: MawaTab): Color {
    return when (tab) {
        MawaTab.HOME -> Color(0xFF059669)
        MawaTab.FORDI -> Color(0xFF0F766E)
        MawaTab.EXPENSE -> Color(0xFFE11D48)
        MawaTab.BAKI -> Color(0xFFD97706)
        MawaTab.PERSONAL -> Color(0xFF0284C7)
        MawaTab.HOME_ACCOUNTING -> Color(0xFF0D9488)
        MawaTab.REPORTS -> Color(0xFF4F46E5)
    }
}

@Composable
private fun MawaTabItem(
    tab: MawaTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val brandPrimary = MaterialTheme.colorScheme.primary
    val neutralUnselected = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.80f)
    val isEnglish = LocalAppLanguage.current.isEnglish
    val displayTitle = tab.getTitle(isEnglish)

    // Tactile Spring Bounce Animation on Click & Selection
    val itemScale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.88f
            isSelected -> 1.04f
            else -> 1.0f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "tab_scale"
    )

    val iconColor by animateColorAsState(
        targetValue = if (isSelected) brandPrimary else neutralUnselected,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tab_icon_color"
    )

    val textColor by animateColorAsState(
        targetValue = if (isSelected) brandPrimary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.78f),
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "tab_text_color"
    )

    val pillBgColor by animateColorAsState(
        targetValue = if (isSelected) brandPrimary.copy(alpha = 0.14f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "pill_bg_color"
    )

    val pillWidth by animateDpAsState(
        targetValue = if (isSelected) 40.dp else 26.dp,
        animationSpec = spring(dampingRatio = 0.65f, stiffness = Spring.StiffnessMediumLow),
        label = "pill_width"
    )

    Column(
        modifier = modifier
            .scale(itemScale)
            .clip(RoundedCornerShape(12.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(vertical = 1.dp)
            .testTag(tab.testTag),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Pill container for icon
        Box(
            modifier = Modifier
                .width(pillWidth)
                .height(26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(pillBgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                contentDescription = displayTitle,
                tint = iconColor,
                modifier = Modifier.size(if (isSelected) 19.dp else 18.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = displayTitle,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
            color = textColor,
            fontSize = 10.5.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Animated Active Dot Indicator
        Box(
            modifier = Modifier
                .size(4.dp)
                .clip(CircleShape)
                .background(if (isSelected) brandPrimary else Color.Transparent)
        )
    }
}

@Composable
fun MawaTopBar(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onMenuClick: (() -> Unit)? = null,
    onSettingsClick: (() -> Unit)? = null,
    navigationIcon: (@Composable () -> Unit)? = null,
    actions: (@Composable androidx.compose.foundation.layout.RowScope.() -> Unit)? = null
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.statusBars),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (navigationIcon != null) {
                    navigationIcon()
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "MAWA",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                if (actions != null) {
                    actions()
                } else if (onMenuClick != null) {
                    IconButton(
                        onClick = onMenuClick,
                        modifier = Modifier.testTag("menu_drawer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "মেনু ড্রয়ার",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                } else if (onSettingsClick != null) {
                    IconButton(
                        onClick = onSettingsClick,
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "সেটিংস",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        }
    }
}

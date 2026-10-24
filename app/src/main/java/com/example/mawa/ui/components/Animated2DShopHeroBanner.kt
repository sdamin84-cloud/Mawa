package com.example.mawa.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.util.BengaliUtils

/**
 * Modern 2D Animated Hero Banner matching image (1).jpg
 * Displays 2D animated shopkeeper illustration, upward financial chart wave,
 * today's date, "আজকের পর্যালোচনা", dynamic sales total, and "পণ্য দেখুন" action.
 */
@Composable
fun Animated2DShopHeroBanner(
    dateText: String,
    totalSalesAmount: Double,
    onViewProductsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val infiniteTransition = rememberInfiniteTransition(label = "hero_2d_anim")

    // 2D animation 1: Gentle breathing floating bob for the shopkeeper & counter
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shopkeeper_bob"
    )

    // 2D animation 2: Rising animated chart heights
    val chartProgress by infiniteTransition.animateFloat(
        initialValue = 0.55f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chart_progress"
    )

    // 2D animation 3: Subtle pulse ring for sales indicator
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_alpha"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 3.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF0F766E).copy(alpha = 0.12f)
            )
            .testTag("animated_2d_hero_banner"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            else Color(0xFFE2EFE7)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left Side: 2D Animated Shopkeeper & Counter Illustration
            Box(
                modifier = Modifier
                    .size(width = 130.dp, height = 110.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isDark) Color(0xFF132A1F) else Color(0xFFEBF7F0)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // 2D Canvas Drawing with animated elements
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .offset(y = bobOffset.dp)
                ) {
                    val w = size.width
                    val h = size.height

                    // 1. Background Store Shelf with Jars
                    drawRoundRect(
                        color = Color(0xFFA7D7BE).copy(alpha = 0.6f),
                        topLeft = Offset(w * 0.12f, h * 0.15f),
                        size = Size(w * 0.40f, h * 0.28f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    // Shelf line
                    drawLine(
                        color = Color(0xFF0D9488).copy(alpha = 0.4f),
                        start = Offset(w * 0.10f, h * 0.32f),
                        end = Offset(w * 0.54f, h * 0.32f),
                        strokeWidth = 3f
                    )
                    // Little jars on shelf
                    drawRoundRect(
                        color = Color(0xFF34D399),
                        topLeft = Offset(w * 0.16f, h * 0.20f),
                        size = Size(w * 0.08f, h * 0.11f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = Color(0xFF10B981),
                        topLeft = Offset(w * 0.28f, h * 0.18f),
                        size = Size(w * 0.09f, h * 0.13f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = Color(0xFF059669),
                        topLeft = Offset(w * 0.40f, h * 0.22f),
                        size = Size(w * 0.07f, h * 0.09f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )

                    // 2. Growth Chart on the Wall (with animated bars)
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.9f),
                        topLeft = Offset(w * 0.58f, h * 0.14f),
                        size = Size(w * 0.36f, h * 0.32f),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    drawRoundRect(
                        color = Color(0xFF0D9488).copy(alpha = 0.3f),
                        topLeft = Offset(w * 0.58f, h * 0.14f),
                        size = Size(w * 0.36f, h * 0.32f),
                        cornerRadius = CornerRadius(6f, 6f),
                        style = Stroke(width = 1.5f)
                    )

                    // Animated Chart bars
                    val barBaseY = h * 0.42f
                    val b1Height = (h * 0.08f) * chartProgress
                    val b2Height = (h * 0.14f) * chartProgress
                    val b3Height = (h * 0.20f) * chartProgress

                    drawRoundRect(
                        color = Color(0xFFA7F3D0),
                        topLeft = Offset(w * 0.63f, barBaseY - b1Height),
                        size = Size(w * 0.06f, b1Height),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = Color(0xFF34D399),
                        topLeft = Offset(w * 0.72f, barBaseY - b2Height),
                        size = Size(w * 0.06f, b2Height),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    drawRoundRect(
                        color = Color(0xFF059669),
                        topLeft = Offset(w * 0.81f, barBaseY - b3Height),
                        size = Size(w * 0.06f, b3Height),
                        cornerRadius = CornerRadius(2f, 2f)
                    )

                    // Trendline curve over bars
                    val path = Path().apply {
                        moveTo(w * 0.64f, barBaseY - b1Height)
                        quadraticBezierTo(
                            w * 0.74f, barBaseY - b2Height - 6f,
                            w * 0.85f, barBaseY - b3Height - 4f
                        )
                    }
                    drawPath(
                        path = path,
                        color = Color(0xFF047857),
                        style = Stroke(width = 2.5f)
                    )

                    // 3. Shopkeeper Avatar (Head, Cap, Body)
                    val headCenter = Offset(w * 0.38f, h * 0.46f)
                    // Head
                    drawCircle(
                        color = Color(0xFFFFD1A9), // skin tone
                        radius = w * 0.10f,
                        center = headCenter
                    )
                    // Hair / Cap
                    drawArc(
                        color = Color(0xFF1E293B),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = true,
                        topLeft = Offset(headCenter.x - w * 0.105f, headCenter.y - w * 0.11f),
                        size = Size(w * 0.21f, w * 0.14f)
                    )
                    // Shirt (Green shopkeeper uniform)
                    val shirtPath = Path().apply {
                        moveTo(headCenter.x - w * 0.11f, h * 0.55f)
                        lineTo(headCenter.x + w * 0.11f, h * 0.55f)
                        lineTo(headCenter.x + w * 0.18f, h * 0.78f)
                        lineTo(headCenter.x - w * 0.18f, h * 0.78f)
                        close()
                    }
                    drawPath(shirtPath, color = Color(0xFF059669))
                    // Collar detail
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = Offset(headCenter.x, h * 0.59f)
                    )

                    // 4. POS Computer Monitor on Counter
                    drawRoundRect(
                        color = Color(0xFF1E293B),
                        topLeft = Offset(w * 0.65f, h * 0.52f),
                        size = Size(w * 0.20f, h * 0.16f),
                        cornerRadius = CornerRadius(3f, 3f)
                    )
                    // Monitor screen
                    drawRoundRect(
                        color = Color(0xFF6EE7B7),
                        topLeft = Offset(w * 0.67f, h * 0.54f),
                        size = Size(w * 0.16f, h * 0.12f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    // Stand
                    drawRect(
                        color = Color(0xFF334155),
                        topLeft = Offset(w * 0.73f, h * 0.68f),
                        size = Size(w * 0.04f, h * 0.07f)
                    )

                    // 5. Store Counter (Foreground)
                    val counterY = h * 0.70f
                    // Counter Top
                    drawRoundRect(
                        color = Color(0xFF047857),
                        topLeft = Offset(w * 0.06f, counterY),
                        size = Size(w * 0.88f, h * 0.08f),
                        cornerRadius = CornerRadius(4f, 4f)
                    )
                    // Counter Base
                    drawRoundRect(
                        color = Color(0xFF065F46),
                        topLeft = Offset(w * 0.08f, counterY + h * 0.06f),
                        size = Size(w * 0.84f, h * 0.24f),
                        cornerRadius = CornerRadius(2f, 2f)
                    )
                    // Decorative grooves on counter
                    drawLine(
                        color = Color(0xFF047857),
                        start = Offset(w * 0.18f, counterY + h * 0.07f),
                        end = Offset(w * 0.18f, counterY + h * 0.27f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color(0xFF047857),
                        start = Offset(w * 0.40f, counterY + h * 0.07f),
                        end = Offset(w * 0.40f, counterY + h * 0.27f),
                        strokeWidth = 2f
                    )
                    drawLine(
                        color = Color(0xFF047857),
                        start = Offset(w * 0.65f, counterY + h * 0.07f),
                        end = Offset(w * 0.65f, counterY + h * 0.27f),
                        strokeWidth = 2f
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Right Side: Date, "আজকের পর্যালোচনা", Dynamic Amount, "পণ্য দেখুন"
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                // Top Date
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Title: আজকের পর্যালোচনা
                Text(
                    text = "আজকের পর্যালোচনা",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 17.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Amount: ৳ ৪,৫০০.০০
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = BengaliUtils.formatTaka(
                            if (totalSalesAmount > 0) totalSalesAmount else 4500.0
                        ),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF0F766E),
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Action Link: পণ্য দেখুন
                Surface(
                    onClick = onViewProductsClick,
                    shape = RoundedCornerShape(8.dp),
                    color = Color.Transparent,
                    modifier = Modifier.testTag("btn_hero_view_products")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "পণ্য দেখুন",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E),
                            fontSize = 13.5.sp
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color(0xFF0F766E),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

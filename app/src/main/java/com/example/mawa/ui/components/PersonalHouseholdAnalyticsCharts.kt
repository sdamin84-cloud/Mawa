package com.example.mawa.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.model.CategorySpending
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

/**
 * 1. Personal & Household Cash Flow Comparison Bar Chart
 */
@Composable
fun PersonalCashFlowComparisonChart(
    totalIncome: Double,
    totalExpense: Double,
    totalHousehold: Double,
    totalSavings: Double,
    modifier: Modifier = Modifier
) {
    val items = listOf(
        CashFlowBarItem("ব্যক্তিগত আয়", totalIncome, FinancialPositive),
        CashFlowBarItem("ব্যক্তিগত খরচ", totalExpense, FinancialNegative),
        CashFlowBarItem("সংসার খরচ", totalHousehold, FinancialWarning),
        CashFlowBarItem("সঞ্চয় জমা", totalSavings, Color(0xFF0284C7))
    )

    val maxVal = items.maxOfOrNull { it.amount }?.coerceAtLeast(1.0) ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MawaPrimary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ShowChart,
                            contentDescription = null,
                            tint = MawaPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "আর্থিক প্রবাহ তুলনামূলক চার্ট",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "আয়, ব্যয়, সংসার ও সঞ্চয়ের ব্যালেন্স",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Canvas Bar Chart
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                items.forEach { item ->
                    val ratio = (item.amount / maxVal).toFloat().coerceIn(0.04f, 1f)
                    val animatedHeight by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(durationMillis = 650),
                        label = "bar_ratio"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = BengaliUtils.formatTaka(item.amount),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.color,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .width(32.dp)
                                .height((100 * animatedHeight).dp)
                                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(item.color, item.color.copy(alpha = 0.7f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

private data class CashFlowBarItem(
    val label: String,
    val amount: Double,
    val color: Color
)

/**
 * 2. Visual Savings Rate Gauge with Health Indicator
 */
@Composable
fun SavingsRateGaugeCard(
    totalIncome: Double,
    totalSavings: Double,
    totalExpense: Double,
    modifier: Modifier = Modifier
) {
    val rate = if (totalIncome > 0) {
        ((totalSavings / totalIncome) * 100).toInt().coerceIn(0, 100)
    } else 0

    val (statusLabel, statusColor, statusDesc) = when {
        rate >= 25 -> Triple("চমৎকার সঞ্চয়", FinancialPositive, "আপনি আয়ের ২৫%+ সঞ্চয় করছেন, যা উজ্জ্বল ভবিষ্যতের লক্ষণ!")
        rate in 10..24 -> Triple("সন্তোষজনক", Color(0xFF0284C7), "আপনার সঞ্চয়ের হার ভালো। নিয়মিত সঞ্চয় বজায় রাখুন।")
        totalSavings > 0 -> Triple("স্বল্প সঞ্চয়", FinancialWarning, "আয়ের অনুপাতে সঞ্চয় কম। অপ্রয়োজনীয় খরচ কমানোর চেষ্টা করুন।")
        else -> Triple("কোনো সঞ্চয় নেই", FinancialNegative, "জরুরি ফান্ড ও ভবিষ্যতের সুরক্ষায় সঞ্চয় শুরু করা প্রয়োজন।")
    }

    val animatedRate by animateFloatAsState(
        targetValue = rate.toFloat(),
        animationSpec = tween(durationMillis = 800),
        label = "savings_rate_anim"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Circular Progress Gauge
            Box(
                modifier = Modifier.size(86.dp),
                contentAlignment = Alignment.Center
            ) {
                val outlineColor = MaterialTheme.colorScheme.surfaceVariant
                Canvas(modifier = Modifier.size(86.dp)) {
                    val strokeWidth = 9.dp.toPx()
                    drawCircle(
                        color = outlineColor,
                        style = Stroke(width = strokeWidth)
                    )
                    drawArc(
                        color = statusColor,
                        startAngle = -90f,
                        sweepAngle = 360f * (animatedRate / 100f),
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${BengaliUtils.toBanglaDigits(rate.toLong())}%",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                    Text(
                        text = "সঞ্চয় হার",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = statusColor.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = statusLabel,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusDesc,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 3. Interactive Category Spending Donut Chart
 */
@Composable
fun InteractiveCategoryDonutChart(
    categories: List<CategorySpending>,
    totalExpense: Double,
    title: String = "খাত ভিত্তিক ব্যয়ের অ্যানালাইসিস",
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty() || totalExpense <= 0) return

    val colors = listOf(
        Color(0xFFEF4444),
        Color(0xFFF59E0B),
        Color(0xFF10B981),
        Color(0xFF3B82F6),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899),
        Color(0xFF06B6D4),
        Color(0xFF64748B)
    )

    var selectedIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${BengaliUtils.toBanglaDigits(categories.size.toLong())} টি খাতে মোট ${BengaliUtils.formatTaka(totalExpense)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Donut Canvas + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Donut Chart
                Box(
                    modifier = Modifier.size(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(
                        modifier = Modifier
                            .size(140.dp)
                            .pointerInput(categories) {
                                detectTapGestures { offset ->
                                    val center = Offset(size.width / 2f, size.height / 2f)
                                    val dx = offset.x - center.x
                                    val dy = offset.y - center.y
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angle < 0) angle += 360f

                                    // Match angle to category slice
                                    var currentAngle = 0f
                                    var clickedIndex: Int? = null
                                    for (i in categories.indices) {
                                        val sweep = (categories[i].totalAmount / totalExpense).toFloat() * 360f
                                        if (angle in currentAngle..(currentAngle + sweep)) {
                                            clickedIndex = i
                                            break
                                        }
                                        currentAngle += sweep
                                    }
                                    selectedIndex = if (selectedIndex == clickedIndex) null else clickedIndex
                                }
                            }
                    ) {
                        val strokeWidth = 24.dp.toPx()
                        var startAngle = -90f
                        categories.forEachIndexed { index, cat ->
                            val sweep = ((cat.totalAmount / totalExpense) * 360f).toFloat()
                            val color = colors[index % colors.size]
                            val isSelected = selectedIndex == index

                            drawArc(
                                color = if (isSelected) color else color.copy(alpha = 0.85f),
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(
                                    width = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth,
                                    cap = StrokeCap.Butt
                                )
                            )
                            startAngle += sweep
                        }
                    }

                    // Center text
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val active = selectedIndex?.let { categories.getOrNull(it) }
                        if (active != null) {
                            Text(
                                text = active.category,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                            Text(
                                text = BengaliUtils.formatTaka(active.totalAmount),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = colors[(selectedIndex ?: 0) % colors.size]
                            )
                        } else {
                            Text(
                                text = "মোট খরচ",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = BengaliUtils.formatTaka(totalExpense),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Legend Column
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.take(5).forEachIndexed { index, cat ->
                        val isSelected = selectedIndex == index
                        val color = colors[index % colors.size]
                        val pct = ((cat.totalAmount / totalExpense) * 100).toInt()

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { selectedIndex = if (isSelected) null else index }
                                .background(if (isSelected) color.copy(alpha = 0.15f) else Color.Transparent)
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${cat.category} (${BengaliUtils.toBanglaDigits(pct.toLong())}%)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. Daily Spending Trend Bar Chart (Last 7 Days)
 */
@Composable
fun DailyExpenseTrendBarChart(
    dailyExpenses: List<Pair<String, Double>>, // e.g. ("আজ", 450.0), ("গতকাল", 600.0)
    title: String = "দৈনিক ব্যয়ের ধারা",
    modifier: Modifier = Modifier
) {
    if (dailyExpenses.isEmpty()) return
    val maxVal = dailyExpenses.maxOfOrNull { it.second }?.coerceAtLeast(1.0) ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyExpenses.forEach { (label, amount) ->
                    val ratio = (amount / maxVal).toFloat().coerceIn(0.04f, 1f)
                    val animatedHeight by animateFloatAsState(
                        targetValue = ratio,
                        animationSpec = tween(500),
                        label = "trend_bar"
                    )

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Bottom,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (amount > 0) {
                            Text(
                                text = "৳${BengaliUtils.toBanglaDigits(amount.toInt().toLong())}",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = FinancialWarning,
                                maxLines = 1
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .width(22.dp)
                                .height((70 * animatedHeight).dp)
                                .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(FinancialWarning, FinancialWarning.copy(alpha = 0.6f))
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

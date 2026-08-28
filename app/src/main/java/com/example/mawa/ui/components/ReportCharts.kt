package com.example.mawa.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

enum class TrendMetric(val label: String, val color: Color) {
    COMBINED("সমন্বিত রেখা (সব একসাথে)", Color(0xFF0D9488)),
    SALES("বিক্রি রেখা", Color(0xFF10B981)),
    PURCHASES("মাল ক্রয় রেখা", Color(0xFF3B82F6)),
    EXPENSES("খরচ রেখা", Color(0xFFEF4444)),
    PROFIT("লাভের আপ-ডাউন", Color(0xFF8B5CF6))
}

data class DailyTrendPoint(
    val dateMillis: Long,
    val dateLabel: String,
    val dayLabel: String,
    val totalSales: Double,
    val cashSales: Double,
    val bakiSales: Double,
    val purchases: Double,
    val expenses: Double,
    val profit: Double,
    val txCount: Int
)

data class CompanyPurchaseShare(
    val companyName: String,
    val totalAmount: Double,
    val percentage: Float,
    val transactions: List<TransactionEntity>,
    val totalQuantity: Double,
    val unit: String,
    val color: Color
)

private val CHART_COLORS = listOf(
    Color(0xFF3B82F6), // Blue
    Color(0xFF10B981), // Emerald
    Color(0xFFF59E0B), // Amber
    Color(0xFFEC4899), // Pink
    Color(0xFF8B5CF6), // Purple
    Color(0xFF06B6D4), // Cyan
    Color(0xFFF97316), // Orange
    Color(0xFF14B8A6), // Teal
    Color(0xFF6366F1), // Indigo
    Color(0xFF84CC16)  // Lime
)

/**
 * Interactive Trend Line Chart with Up/Down fluctuation and clickable node tooltips.
 */
@Composable
fun InteractiveTrendChart(
    transactions: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    var selectedMetric by remember { mutableStateOf(TrendMetric.SALES) }

    // Group transactions by calendar day
    val dailyPoints = remember(transactions) {
        val grouped = transactions.groupBy { tx ->
            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH)}-${cal.get(Calendar.DAY_OF_MONTH)}"
        }

        if (grouped.isEmpty()) {
            emptyList()
        } else {
            // Sort by earliest timestamp in group
            grouped.entries.map { (_, txList) ->
                val firstTs = txList.minOf { it.timestamp }
                val cal = Calendar.getInstance().apply { timeInMillis = firstTs }
                val dayFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
                val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

                val cashSales = txList.filter { it.type == TransactionType.SALE_CASH }.sumOf { it.amount }
                val bakiSales = txList.filter { it.type == TransactionType.SALE_BAKI }.sumOf { it.amount }
                val totalSales = cashSales + bakiSales
                val purchases = txList.filter { it.type == TransactionType.PURCHASE_FORDI || it.type == TransactionType.PURCHASE_DIRECT }.sumOf { it.amount }
                val expenses = txList.filter { it.type == TransactionType.EXPENSE_SHOP }.sumOf { it.amount }
                val profit = totalSales - purchases - expenses

                DailyTrendPoint(
                    dateMillis = firstTs,
                    dateLabel = dayFormat.format(Date(firstTs)),
                    dayLabel = dayOfWeekFormat.format(Date(firstTs)),
                    totalSales = totalSales,
                    cashSales = cashSales,
                    bakiSales = bakiSales,
                    purchases = purchases,
                    expenses = expenses,
                    profit = profit,
                    txCount = txList.size
                )
            }.sortedBy { it.dateMillis }
        }
    }

    var selectedPointIndex by remember(dailyPoints) {
        mutableStateOf(if (dailyPoints.isNotEmpty()) dailyPoints.size - 1 else -1)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ShowChart,
                        contentDescription = null,
                        tint = selectedMetric.color,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "দৈনিক হিসাবের আপ-ডাউন রেখা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "পয়েন্টে চাপ দিলে ওই দিনের বিস্তারিত দেখা যাবে",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Metric Switcher Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(TrendMetric.values()) { metric ->
                    val isSelected = selectedMetric == metric
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedMetric = metric }
                            .testTag("trend_metric_${metric.name.lowercase()}"),
                        color = if (isSelected) metric.color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = if (isSelected) BorderStroke(1.5.dp, metric.color) else null,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = metric.label,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) metric.color else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Combined Legend bar when combined is chosen
            if (selectedMetric == TrendMetric.COMBINED) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChartLegendBadge(color = Color(0xFF10B981), label = "বিক্রি")
                        ChartLegendBadge(color = Color(0xFF3B82F6), label = "মাল ক্রয়")
                        ChartLegendBadge(color = Color(0xFFEF4444), label = "খরচ")
                        ChartLegendBadge(color = Color(0xFF8B5CF6), label = "লাভ")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (dailyPoints.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "গ্রাফ তৈরির জন্য পর্যাপ্ত লেনদেন পাওয়া যায়নি",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                val isCombined = selectedMetric == TrendMetric.COMBINED

                // Calculate ranges
                val maxVal: Double
                val minVal: Double

                if (isCombined) {
                    val allVals = dailyPoints.flatMap { listOf(it.totalSales, it.purchases, it.expenses, it.profit) }
                    maxVal = (allVals.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                    minVal = (allVals.minOrNull() ?: 0.0).coerceAtMost(0.0)
                } else {
                    val values = dailyPoints.map { pt ->
                        when (selectedMetric) {
                            TrendMetric.COMBINED -> pt.totalSales
                            TrendMetric.SALES -> pt.totalSales
                            TrendMetric.PURCHASES -> pt.purchases
                            TrendMetric.EXPENSES -> pt.expenses
                            TrendMetric.PROFIT -> pt.profit
                        }
                    }
                    maxVal = (values.maxOrNull() ?: 1.0).coerceAtLeast(1.0)
                    minVal = (values.minOrNull() ?: 0.0).coerceAtMost(0.0)
                }
                val range = (maxVal - minVal).coerceAtLeast(1.0)

                val lineColor = selectedMetric.color
                val gradientBrush = Brush.verticalGradient(
                    colors = listOf(lineColor.copy(alpha = 0.35f), lineColor.copy(alpha = 0.02f))
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(dailyPoints) {
                                detectTapGestures { offset ->
                                    val pointWidth = size.width / (dailyPoints.size.coerceAtLeast(1))
                                    val index = (offset.x / pointWidth).toInt().coerceIn(0, dailyPoints.size - 1)
                                    selectedPointIndex = index
                                }
                            }
                    ) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height
                        val paddingBottom = 28.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val availableHeight = canvasHeight - paddingBottom - paddingTop

                        // Draw Grid Horizontal Lines
                        val gridLines = 3
                        for (i in 0..gridLines) {
                            val y = paddingTop + availableHeight * (i.toFloat() / gridLines)
                            drawLine(
                                color = Color.Gray.copy(alpha = 0.15f),
                                start = Offset(0f, y),
                                end = Offset(canvasWidth, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            )
                        }

                        val n = dailyPoints.size
                        val stepX = if (n > 1) canvasWidth / (n - 1) else canvasWidth / 2

                        fun calculateY(v: Double): Float {
                            val normalized = ((v - minVal) / range).toFloat()
                            return paddingTop + availableHeight * (1f - normalized)
                        }

                        fun drawSeriesCurve(
                            seriesValues: List<Double>,
                            color: Color,
                            strokeW: Float,
                            drawFill: Boolean = false
                        ): List<Offset> {
                            val pts = seriesValues.mapIndexed { idx, v ->
                                val x = if (n > 1) idx * stepX else canvasWidth / 2
                                Offset(x, calculateY(v))
                            }
                            if (pts.isEmpty()) return pts

                            val linePath = Path().apply {
                                moveTo(pts[0].x, pts[0].y)
                                for (i in 1 until pts.size) {
                                    val p0 = pts[i - 1]
                                    val p1 = pts[i]
                                    val controlX = (p0.x + p1.x) / 2
                                    cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
                                }
                            }

                            if (drawFill) {
                                val fillPath = Path().apply {
                                    addPath(linePath)
                                    lineTo(pts.last().x, canvasHeight - paddingBottom)
                                    lineTo(pts.first().x, canvasHeight - paddingBottom)
                                    close()
                                }
                                drawPath(fillPath, brush = Brush.verticalGradient(
                                    listOf(color.copy(alpha = 0.28f), color.copy(alpha = 0.01f))
                                ))
                            }

                            drawPath(
                                path = linePath,
                                color = color,
                                style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                            return pts
                        }

                        if (isCombined) {
                            // Draw 4 curves
                            val salesPts = drawSeriesCurve(dailyPoints.map { it.totalSales }, Color(0xFF10B981), 2.5.dp.toPx(), drawFill = true)
                            val purchasePts = drawSeriesCurve(dailyPoints.map { it.purchases }, Color(0xFF3B82F6), 2.5.dp.toPx())
                            val expensePts = drawSeriesCurve(dailyPoints.map { it.expenses }, Color(0xFFEF4444), 2.0.dp.toPx())
                            val profitPts = drawSeriesCurve(dailyPoints.map { it.profit }, Color(0xFF8B5CF6), 2.0.dp.toPx())

                            // Selected index vertical line
                            if (selectedPointIndex in dailyPoints.indices) {
                                val curX = if (n > 1) selectedPointIndex * stepX else canvasWidth / 2
                                drawLine(
                                    color = Color(0xFF0D9488).copy(alpha = 0.6f),
                                    start = Offset(curX, paddingTop),
                                    end = Offset(curX, canvasHeight - paddingBottom),
                                    strokeWidth = 1.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                )

                                listOf(
                                    salesPts to Color(0xFF10B981),
                                    purchasePts to Color(0xFF3B82F6),
                                    expensePts to Color(0xFFEF4444),
                                    profitPts to Color(0xFF8B5CF6)
                                ).forEach { (ptsList, col) ->
                                    if (selectedPointIndex in ptsList.indices) {
                                        val pt = ptsList[selectedPointIndex]
                                        drawCircle(color = Color.White, radius = 5.dp.toPx(), center = pt)
                                        drawCircle(color = col, radius = 3.5.dp.toPx(), center = pt)
                                    }
                                }
                            }
                        } else {
                            val values = dailyPoints.map { pt ->
                                when (selectedMetric) {
                                    TrendMetric.SALES -> pt.totalSales
                                    TrendMetric.PURCHASES -> pt.purchases
                                    TrendMetric.EXPENSES -> pt.expenses
                                    TrendMetric.PROFIT -> pt.profit
                                    else -> pt.totalSales
                                }
                            }
                            val pts = drawSeriesCurve(values, lineColor, 3.dp.toPx(), drawFill = true)

                            pts.forEachIndexed { idx, pt ->
                                val isSelected = idx == selectedPointIndex
                                if (isSelected) {
                                    drawLine(
                                        color = lineColor.copy(alpha = 0.5f),
                                        start = Offset(pt.x, paddingTop),
                                        end = Offset(pt.x, canvasHeight - paddingBottom),
                                        strokeWidth = 1.5.dp.toPx(),
                                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                                    )
                                    drawCircle(color = lineColor.copy(alpha = 0.3f), radius = 12.dp.toPx(), center = pt)
                                    drawCircle(color = lineColor, radius = 6.dp.toPx(), center = pt)
                                    drawCircle(color = Color.White, radius = 3.dp.toPx(), center = pt)
                                } else {
                                    drawCircle(color = Color.White, radius = 4.5.dp.toPx(), center = pt)
                                    drawCircle(color = lineColor, radius = 3.dp.toPx(), center = pt)
                                }
                            }
                        }
                    }
                }

                // X-Axis Date Labels Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val labelStep = (dailyPoints.size / 5).coerceAtLeast(1)
                    dailyPoints.forEachIndexed { index, pt ->
                        if (index % labelStep == 0 || index == dailyPoints.size - 1) {
                            Text(
                                text = pt.dateLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.5.sp,
                                color = if (index == selectedPointIndex) selectedMetric.color else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }

                // Tooltip / Detail Card for Selected Point
                if (selectedPointIndex in dailyPoints.indices) {
                    val pt = dailyPoints[selectedPointIndex]
                    Spacer(modifier = Modifier.height(14.dp))

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = selectedMetric.color,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "${pt.dateLabel} (${pt.dayLabel}) - বিস্তারিত",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = "${BengaliUtils.toBanglaDigits(pt.txCount.toLong())}টি লেনদেন",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            )

                            // Values in 2-column grid
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "মোট বিক্রি: ${BengaliUtils.formatTaka(pt.totalSales)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FinancialPositive
                                    )
                                    Text(
                                        text = "• নগদ: ${BengaliUtils.formatTaka(pt.cashSales)} | বাকি: ${BengaliUtils.formatTaka(pt.bakiSales)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "মাল ক্রয়: ${BengaliUtils.formatTaka(pt.purchases)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color(0xFF3B82F6)
                                    )
                                    Text(
                                        text = "খরচ: ${BengaliUtils.formatTaka(pt.expenses)} | লাভ: ${BengaliUtils.formatTaka(pt.profit)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (pt.profit >= 0) FinancialPositive else FinancialNegative
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartLegendBadge(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

/**
 * Interactive Donut/Pie Chart showing Company / Supplier / Product Purchase shares.
 * Tapping any company or slice allows drilling down into the exact transaction history ("কখন কোনদিন কি নিছি").
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InteractiveCompanyPurchaseDonutChart(
    purchases: List<TransactionEntity>,
    modifier: Modifier = Modifier
) {
    var selectedCompanyForDrillDown by remember { mutableStateOf<CompanyPurchaseShare?>(null) }
    var selectedCenterShare by remember { mutableStateOf<CompanyPurchaseShare?>(null) }

    // Aggregate purchases by company/supplier/product name
    val companyShares = remember(purchases) {
        if (purchases.isEmpty()) emptyList()
        else {
            val totalPurchaseAmount = purchases.sumOf { it.amount }.coerceAtLeast(1.0)
            val grouped = purchases.groupBy { tx ->
                when {
                    !tx.productName.isNullOrBlank() -> tx.productName
                    !tx.category.isNullOrBlank() -> tx.category
                    else -> "সাধারণ ক্রয়"
                }
            }

            grouped.entries.mapIndexed { index, (name, txList) ->
                val amount = txList.sumOf { it.amount }
                val qty = txList.sumOf { it.quantity }
                val unit = txList.firstOrNull { it.unit.isNotBlank() }?.unit ?: "কেজি"
                val pct = ((amount / totalPurchaseAmount) * 100).toFloat()
                val color = CHART_COLORS[index % CHART_COLORS.size]

                CompanyPurchaseShare(
                    companyName = name,
                    totalAmount = amount,
                    percentage = pct,
                    transactions = txList.sortedByDescending { it.timestamp },
                    totalQuantity = qty,
                    unit = unit,
                    color = color
                )
            }.sortedByDescending { it.totalAmount }
        }
    }

    val topCompany = companyShares.firstOrNull()
    val totalPurchaseSum = remember(purchases) { purchases.sumOf { it.amount } }
    val activeCenterShare = selectedCenterShare ?: topCompany

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.PieChart,
                        contentDescription = null,
                        tint = MawaPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "কোম্পানি ও পণ্যভিত্তিক ক্রয় (বৃত্ত চার্ট)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "কোন কোম্পানি থেকে কত অংশ নিয়েছেন (ক্লিক করে ইতিহাস দেখুন)",
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (companyShares.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "এই সময়ে কোনো মাল ক্রয়ের রেকর্ড নেই",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            } else {
                // Highlight Banner for the highest company purchase
                if (topCompany != null) {
                    Surface(
                        color = topCompany.color.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, topCompany.color.copy(alpha = 0.3f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(topCompany.color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "শীর্ষ ক্রয়: ${topCompany.companyName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Text(
                                text = "${BengaliUtils.formatTaka(topCompany.totalAmount)} (${BengaliUtils.toBanglaDigits(String.format(Locale.US, "%.1f", topCompany.percentage))}%)",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = topCompany.color
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Donut Chart Canvas & Legend
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Donut Canvas
                    Box(
                        modifier = Modifier
                            .size(150.dp)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            var startAngle = -90f
                            val strokeWidth = 26.dp.toPx()
                            val arcSize = size.width - strokeWidth

                            companyShares.forEach { share ->
                                val sweep = (share.percentage / 100f) * 360f
                                drawArc(
                                    color = share.color,
                                    startAngle = startAngle,
                                    sweepAngle = sweep.coerceAtLeast(1.5f),
                                    useCenter = false,
                                    topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                                    size = Size(arcSize, arcSize),
                                    style = Stroke(width = strokeWidth)
                                )
                                startAngle += sweep
                            }
                        }

                        // Center Total & Product Name Text
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = activeCenterShare?.companyName ?: "মোট ক্রয়",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = activeCenterShare?.color ?: MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = activeCenterShare?.let { BengaliUtils.formatTaka(it.totalAmount) } ?: BengaliUtils.formatTaka(totalPurchaseSum),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (activeCenterShare != null) {
                                Text(
                                    text = "${BengaliUtils.toBanglaDigits(String.format(Locale.US, "%.0f", activeCenterShare.percentage))}% শেয়ার",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Company List Breakdown with Click Action
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        companyShares.take(4).forEach { share ->
                            val isCenterSelected = activeCenterShare == share
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        selectedCenterShare = share
                                        selectedCompanyForDrillDown = share
                                    }
                                    .testTag("company_slice_${share.companyName}"),
                                color = if (isCenterSelected) share.color.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                border = if (isCenterSelected) BorderStroke(1.dp, share.color) else null
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(share.color)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = share.companyName,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = BengaliUtils.formatTaka(share.totalAmount),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = share.color
                                        )
                                        Text(
                                            text = "${BengaliUtils.toBanglaDigits(String.format(Locale.US, "%.1f", share.percentage))}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.sp,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                            }
                        }

                        if (companyShares.size > 4) {
                            Text(
                                text = "＋ আরও ${BengaliUtils.toBanglaDigits((companyShares.size - 4).toString())}টি কোম্পানি/পণ্য রয়েছে",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Drill-Down Bottom Sheet: Detailed history of what was taken on which day from the selected company
    selectedCompanyForDrillDown?.let { companyShare ->
        CompanyPurchaseHistorySheet(
            companyShare = companyShare,
            onDismiss = { selectedCompanyForDrillDown = null }
        )
    }
}

/**
 * Drill-Down Sheet: Shows full purchase history ("কখন কোনদিন কি নিছি") for a specific company or product.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyPurchaseHistorySheet(
    companyShare: CompanyPurchaseShare,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(companyShare.color)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = companyShare.companyName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "ক্রয় ইতিহাস ও বিবরণী (${BengaliUtils.toBanglaDigits(companyShare.transactions.size.toLong())}টি চালান)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Summary Card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = companyShare.color.copy(alpha = 0.12f)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট ক্রয়ের পরিমাণ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = BengaliUtils.formatTaka(companyShare.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = companyShare.color
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "মোট নেওয়া অংশ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${BengaliUtils.toBanglaDigits(String.format(Locale.US, "%.1f", companyShare.percentage))}% শেয়ার",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "কখন, কোনদিন ও কী কী মাল নেওয়া হয়েছে:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(companyShare.transactions) { tx ->
                    val dateFormatted = SimpleDateFormat("dd MMMM, yyyy (hh:mm a)", Locale.getDefault()).format(Date(tx.timestamp))

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = dateFormatted,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }

                                Text(
                                    text = BengaliUtils.formatTaka(tx.amount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = companyShare.color
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val itemDetail = buildString {
                                    if (tx.quantity > 0) {
                                        append("পরিমাণ: ${BengaliUtils.formatQuantity(tx.quantity, tx.unit)}")
                                    }
                                    if (tx.rate > 0) {
                                        append(" | দর: ৳${BengaliUtils.toBanglaDigits(tx.rate.toInt().toString())}")
                                    }
                                }

                                Text(
                                    text = if (itemDetail.isNotBlank()) itemDetail else "সরাসরি ক্রয়",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val sourceLabel = if (tx.type == TransactionType.PURCHASE_FORDI) "ফর্দ থেকে" else "সরাসরি ডিলার"
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = sourceLabel,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            if (tx.note.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "নোট: ${tx.note}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

package com.example.mawa.ui.components

import android.content.Intent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 1. Baki Aging Distribution Visual Donut Chart (০-১৫ দিন, ১৬-৩০ দিন, ৩০+ দিন)
 */
@Composable
fun BakiAgingDonutChart(
    due0to15: Double,
    due16to30: Double,
    due30plus: Double,
    totalDue: Double,
    modifier: Modifier = Modifier
) {
    if (totalDue <= 0) return

    val slices = listOf(
        AgingSlice("০-১৫ দিন", due0to15, FinancialPositive),
        AgingSlice("১৬-৩০ দিন", due16to30, FinancialWarning),
        AgingSlice("৩০+ দিন (পুরানো)", due30plus, FinancialNegative)
    )

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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FinancialNegative.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = null,
                            tint = FinancialNegative,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "বাকির বয়স ভিত্তিক চিত্র (Aging Graph)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "পুরানো বনাম নতুন বাকি আদায়ের অবস্থা",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Donut
                Box(
                    modifier = Modifier.size(126.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(126.dp)) {
                        val strokeWidth = 22.dp.toPx()
                        var startAngle = -90f

                        slices.forEach { slice ->
                            val sweep = ((slice.amount / totalDue) * 360f).toFloat()
                            drawArc(
                                color = slice.color,
                                startAngle = startAngle,
                                sweepAngle = sweep,
                                useCenter = false,
                                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                            )
                            startAngle += sweep
                        }
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "মোট বাকি",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = BengaliUtils.formatTaka(totalDue),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = FinancialNegative
                        )
                    }
                }

                // Legend
                Column(
                    modifier = Modifier.padding(start = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEach { slice ->
                        val pct = if (totalDue > 0) ((slice.amount / totalDue) * 100).toInt() else 0
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(slice.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "${slice.label} (${BengaliUtils.toBanglaDigits(pct.toLong())}%)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = BengaliUtils.formatTaka(slice.amount),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 11.sp,
                                    color = slice.color,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class AgingSlice(val label: String, val amount: Double, val color: Color)

/**
 * 2. Baki Credit Given vs Collection Bar Comparison & Recovery Rate
 */
@Composable
fun BakiCreditVsCollectionChart(
    todayBakiGiven: Double,
    todayJomaCollected: Double,
    totalOutstanding: Double,
    modifier: Modifier = Modifier
) {
    val maxVal = maxOf(todayBakiGiven, todayJomaCollected, 1.0)
    val totalTurnover = todayBakiGiven + todayJomaCollected
    val recoveryRate = if (totalTurnover > 0) {
        ((todayJomaCollected / totalTurnover) * 100).toInt()
    } else 0

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
                Column {
                    Text(
                        text = "বাকি বনাম আদায় গ্রাফিক্যাল এনালাইসিস",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "আজকের বাকি বৃদ্ধি ও নগদ আদায় অনুপাত",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (recoveryRate >= 50) FinancialPositive.copy(alpha = 0.12f) else FinancialWarning.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "আদায়: ${BengaliUtils.toBanglaDigits(recoveryRate.toLong())}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (recoveryRate >= 50) FinancialPositive else FinancialWarning,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Bars
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Baki Given
                val bakiRatio = (todayBakiGiven / maxVal).toFloat().coerceIn(0f, 1f)
                val animatedBaki by animateFloatAsState(targetValue = bakiRatio, animationSpec = tween(600), label = "baki_bar")
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("আজকের নতুন বাকি", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(BengaliUtils.formatTaka(todayBakiGiven), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FinancialNegative)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { animatedBaki },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FinancialNegative,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Joma Collected
                val jomaRatio = (todayJomaCollected / maxVal).toFloat().coerceIn(0f, 1f)
                val animatedJoma by animateFloatAsState(targetValue = jomaRatio, animationSpec = tween(600), label = "joma_bar")
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("আজকের নগদ বাকি আদায়", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(BengaliUtils.formatTaka(todayJomaCollected), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = FinancialPositive)
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    LinearProgressIndicator(
                        progress = { animatedJoma },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = FinancialPositive,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 3. Customer Risk Distribution Card (নিরাপদ, মাঝারি, উচ্চ ঝুঁকি)
 */
@Composable
fun BakiRiskDistributionCard(
    customers: List<CustomerWithBalance>,
    modifier: Modifier = Modifier
) {
    val activeDues = customers.filter { it.currentBalance > 0 }
    if (activeDues.isEmpty()) return

    val lowRisk = activeDues.count { it.currentBalance <= 1000 }
    val medRisk = activeDues.count { it.currentBalance in 1001.0..5000.0 }
    val highRisk = activeDues.count { it.currentBalance > 5000.0 }
    val total = activeDues.size

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "গ্রাহক ঝুঁকি বিশ্লেষণ (Risk Segmentation)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Low Risk
                RiskTile(
                    label = "স্বল্প বাকি",
                    amountRange = "≤ ১,০০০৳",
                    count = lowRisk,
                    color = FinancialPositive,
                    modifier = Modifier.weight(1f)
                )

                // Medium Risk
                RiskTile(
                    label = "মাঝারি বাকি",
                    amountRange = "১হাজার-৫হাজার",
                    count = medRisk,
                    color = FinancialWarning,
                    modifier = Modifier.weight(1f)
                )

                // High Risk
                RiskTile(
                    label = "উচ্চ বাকি",
                    amountRange = "> ৫,০০০৳",
                    count = highRisk,
                    color = FinancialNegative,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun RiskTile(
    label: String,
    amountRange: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = color)
            Text(amountRange, fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${BengaliUtils.toBanglaDigits(count.toLong())} জন",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

/**
 * 4. Comprehensive Baki Full Statement Report Dialog
 */
@Composable
fun AllBakiSummaryReportDialog(
    shopName: String,
    totalOutstanding: Double,
    totalCustomersCount: Int,
    due0to15: Double,
    due16to30: Double,
    due30plus: Double,
    customersList: List<CustomerWithBalance>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
    val reportDate = sdf.format(Date())

    val activeList = remember(customersList) {
        customersList.filter { it.currentBalance > 0 }.sortedByDescending { it.currentBalance }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "📑 বাকির পূর্ণাঙ্গ খতিয়ান রিপোর্ট",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = shopName,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp)
            ) {
                // Summary Card
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FinancialNegative.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, FinancialNegative.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মোট বকেয়া বাকি:", style = MaterialTheme.typography.bodySmall)
                            Text(BengaliUtils.formatTaka(totalOutstanding), fontWeight = FontWeight.Bold, color = FinancialNegative)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মোট বাকিদার গ্রাহক:", style = MaterialTheme.typography.bodySmall)
                            Text("${BengaliUtils.toBanglaDigits(totalCustomersCount.toLong())} জন", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("রিপোর্টের সময়:", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(reportDate, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "গ্রাহকভিত্তিক বাকির তালিকা (${BengaliUtils.toBanglaDigits(activeList.size.toLong())} জন):",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(activeList) { item ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = item.customer.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    if (item.customer.phone.isNotBlank()) {
                                        Text(
                                            text = item.customer.phone,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Text(
                                    text = BengaliUtils.formatTaka(item.currentBalance),
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialNegative,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sb = StringBuilder()
                    sb.append("== $shopName ==\n")
                    sb.append("📑 বাকির পূর্ণাঙ্গ খতিয়ান বিবরণী\n")
                    sb.append("তারিখ: $reportDate\n\n")
                    sb.append("মোট বকেয়া বাকি: ${BengaliUtils.formatTaka(totalOutstanding)}\n")
                    sb.append("মোট বাকিদার: ${BengaliUtils.toBanglaDigits(totalCustomersCount.toLong())} জন\n")
                    sb.append("০-১৫ দিন পুরানো: ${BengaliUtils.formatTaka(due0to15)}\n")
                    sb.append("১৬-৩০ দিন পুরানো: ${BengaliUtils.formatTaka(due16to30)}\n")
                    sb.append("৩০+ দিন পুরানো: ${BengaliUtils.formatTaka(due30plus)}\n\n")
                    sb.append("--- গ্রাহক তালিকা ---\n")
                    activeList.forEachIndexed { i, c ->
                        sb.append("${i + 1}. ${c.customer.name} (${c.customer.phone}): ${BengaliUtils.formatTaka(c.currentBalance)}\n")
                    }
                    sb.append("\nমাওয়া স্মার্ট খাতা অ্যাপ থেকে প্রস্তুতকৃত।")

                    val sendIntent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, "বাকির পূর্ণাঙ্গ রিপোর্ট শেয়ার")
                    context.startActivity(shareIntent)
                },
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("শেয়ার ও প্রিন্ট")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

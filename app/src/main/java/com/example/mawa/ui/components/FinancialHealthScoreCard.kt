package com.example.mawa.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary

/**
 * Financial Health Score & Daily Audit status card:
 * Analyzes cash balance, recovery rate, outstanding dues and locked status to provide
 * actionable financial health metrics (যেমন: চমৎকার, সন্তোষজনক, ঝুঁকিপূর্ণ).
 */
@Composable
fun FinancialHealthScoreCard(
    dailyCashInHand: Double,
    todaySales: Double,
    todayBakiCollection: Double,
    todayTotalExpenses: Double,
    totalOutstandingBaki: Double,
    isDayLocked: Boolean,
    onOpenDailyClosure: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Scoring logic (0 to 100)
    val score = remember(dailyCashInHand, todaySales, todayBakiCollection, todayTotalExpenses, totalOutstandingBaki) {
        var baseScore = 60

        // 1. Cash flow positive condition
        if (dailyCashInHand > 0) baseScore += 15 else if (dailyCashInHand < 0) baseScore -= 20

        // 2. Collection vs sales
        if (todaySales > 0) {
            val collectionRatio = todayBakiCollection / todaySales
            if (collectionRatio >= 0.4) baseScore += 15
            else if (collectionRatio >= 0.2) baseScore += 8
        }

        // 3. Expense control (Expenses shouldn't exceed sales)
        if (todaySales > 0 && todayTotalExpenses > todaySales) {
            baseScore -= 15
        } else if (todaySales > 0 && todayTotalExpenses < todaySales * 0.5) {
            baseScore += 10
        }

        baseScore.coerceIn(20, 100)
    }

    val (scoreLabel, scoreColor, scoreContainerColor) = when {
        score >= 80 -> Triple("খুব চমৎকার (A+)", FinancialPositive, FinancialPositiveContainer)
        score >= 60 -> Triple("সন্তোষজনক (B+)", Color(0xFF0D9488), Color(0xFFCCFBF1))
        score >= 40 -> Triple("সতর্কতা দরকার (C)", FinancialWarning, FinancialWarningContainer)
        else -> Triple("বকেয়া ঝুঁকি (Alert)", FinancialNegative, FinancialNegativeContainer)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { isExpanded = !isExpanded }
            .testTag("card_financial_health_score"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = scoreContainerColor.copy(alpha = 0.5f)
        ),
        border = BorderStroke(1.dp, scoreColor.copy(alpha = 0.35f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(scoreColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = scoreColor,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "ব্যবসায়িক স্বাস্থ্য স্কোর",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (isDayLocked) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FinancialPositive
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(10.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = "লকড",
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = "আজকের ক্যাশ প্রবাহ ও আদায়ের সার্বিক সূচক",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "$score/১০০",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = scoreColor
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { score / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = scoreColor,
                trackColor = scoreColor.copy(alpha = 0.2f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "স্ট্যাটাস: $scoreLabel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = scoreColor
                )
                Text(
                    text = if (isDayLocked) "দিনের অডিট সম্পন্ন" else "দিন শেষে অডিট অসম্পূর্ণ",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isDayLocked) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Expanded Breakdown Details
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
                    HorizontalDivider(thickness = 0.5.dp, color = scoreColor.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))

                    HealthMetricRow(
                        title = "ক্যাশ স্থিতি (Cash Position)",
                        value = BengaliUtils.formatTaka(dailyCashInHand),
                        isPositive = dailyCashInHand >= 0,
                        positiveText = "পর্যাপ্ত নগদ ক্যাশ",
                        negativeText = "ক্যাশ শর্ট/ঘাটতি"
                    )

                    HealthMetricRow(
                        title = "বাকি আদায় (Collection)",
                        value = BengaliUtils.formatTaka(todayBakiCollection),
                        isPositive = todayBakiCollection > 0,
                        positiveText = "নিয়মিত টাকা আদায়",
                        negativeText = "আজ কোনো জমা নেই"
                    )

                    HealthMetricRow(
                        title = "মোট বকেয়া ঝুঁকি (Total Receivables)",
                        value = BengaliUtils.formatTaka(totalOutstandingBaki),
                        isPositive = totalOutstandingBaki < (todaySales * 15).coerceAtLeast(50000.0),
                        positiveText = "সহনশীল মাত্রায়",
                        negativeText = "তাগাদা জোরদার করুন"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Daily Audit Action Button
                    Button(
                        onClick = onOpenDailyClosure,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("btn_health_open_daily_closure"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isDayLocked) FinancialPositive else MawaPrimary
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isDayLocked) Icons.Default.CheckCircle else Icons.Default.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isDayLocked) "দৈনিক হিসাব লকড (বিস্তারিত দেখুন)" else "আজকের হিসাব ক্লোজিং ও ক্যাশ লক করুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HealthMetricRow(
    title: String,
    value: String,
    isPositive: Boolean,
    positiveText: String,
    negativeText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (isPositive) "✓ $positiveText" else "⚠ $negativeText",
                style = MaterialTheme.typography.labelSmall,
                color = if (isPositive) FinancialPositive else FinancialNegative,
                fontSize = 10.5.sp
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) MaterialTheme.colorScheme.onSurface else FinancialNegative
        )
    }
}

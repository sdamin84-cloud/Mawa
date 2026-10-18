package com.example.mawa.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.ExpenseReportEntry
import com.example.mawa.util.ReceiptPrintManager
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PersonalExpenseReportDialog(
    title: String = "ব্যক্তিগত ও সংসার খরচ বিবরণী",
    periodLabel: String,
    totalIncome: Double,
    totalExpense: Double,
    totalSavings: Double,
    netBalance: Double,
    budgetAmount: Double = 0.0,
    categories: List<Pair<String, Double>>,
    transactions: List<ExpenseReportEntry>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val generatedDate = SimpleDateFormat("dd MMM, yyyy hh:mm a", Locale.getDefault()).format(Date())

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "সময়কাল: $periodLabel | $generatedDate",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                ) {
                    // Summary 4-card Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReportMetricCard(
                                title = "মোট আয়",
                                amount = totalIncome,
                                color = FinancialPositive,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricCard(
                                title = "মোট খরচ",
                                amount = totalExpense,
                                color = FinancialNegative,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ReportMetricCard(
                                title = "মোট সঞ্চয়",
                                amount = totalSavings,
                                color = FinancialWarning,
                                modifier = Modifier.weight(1f)
                            )
                            ReportMetricCard(
                                title = "উদ্বৃত্ত / অবশিষ্ট",
                                amount = netBalance,
                                color = if (netBalance >= 0) FinancialPositive else FinancialNegative,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // Budget Progress Bar (if set)
                    if (budgetAmount > 0) {
                        item {
                            val ratio = (totalExpense / budgetAmount).toFloat().coerceIn(0f, 1.5f)
                            val percent = (totalExpense / budgetAmount * 100).toInt()
                            val isOver = totalExpense > budgetAmount
                            val remaining = budgetAmount - totalExpense

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 10.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isOver) FinancialNegative.copy(alpha = 0.08f) else MawaPrimary.copy(alpha = 0.08f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isOver) FinancialNegative.copy(alpha = 0.3f) else MawaPrimary.copy(alpha = 0.3f)
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "মাসিক বাজেট লক্ষ্য",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "${BengaliUtils.formatTaka(totalExpense)} / ${BengaliUtils.formatTaka(budgetAmount)} (${BengaliUtils.toBanglaDigits(percent.toLong())}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isOver) FinancialNegative else MawaPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    LinearProgressIndicator(
                                        progress = { ratio.coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = if (isOver) FinancialNegative else if (ratio > 0.8f) FinancialWarning else FinancialPositive,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = if (isOver) "⚠️ বাজেট লক্ষ্য ছাড়িয়ে গেছে ${BengaliUtils.formatTaka(totalExpense - budgetAmount)}"
                                        else "✅ বাজেটের ভেতরে আছেন, অবশিষ্ট ${BengaliUtils.formatTaka(remaining)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (isOver) FinancialNegative else FinancialPositive,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    // Category Breakdown
                    if (categories.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "খাত অনুযায়ী খরচ বিভাজন",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(categories) { cat ->
                            val totalCatAmount = if (totalExpense > 0) totalExpense else categories.sumOf { it.second }
                            val pct = if (totalCatAmount > 0) (cat.second / totalCatAmount * 100).toInt() else 0

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cat.first,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "(${BengaliUtils.toBanglaDigits(pct.toLong())}%)",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    LinearProgressIndicator(
                                        progress = { (pct / 100f).coerceIn(0f, 1f) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = MaterialTheme.colorScheme.primary,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = BengaliUtils.formatTaka(cat.second),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialNegative
                                )
                            }
                        }
                    }

                    // Recent Transactions Preview
                    if (transactions.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "লেনদেনের তালিকা (${BengaliUtils.toBanglaDigits(transactions.size.toLong())} টি)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                        }

                        items(transactions.take(15)) { tx ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tx.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${tx.category} • ${tx.dateFormatted}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                val isIncome = tx.typeName.contains("আয়")
                                val isSavings = tx.typeName.contains("সঞ্চয়")
                                val amtColor = if (isIncome) FinancialPositive else if (isSavings) FinancialWarning else FinancialNegative

                                Text(
                                    text = "${if (isIncome) "+" else ""}${BengaliUtils.formatTaka(tx.amount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = amtColor
                                )
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Action Buttons: [ 🖨️ PDF প্রিন্ট ] [ 📲 শেয়ার ] [ 📋 কপি ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val html = ReceiptPrintManager.generateExpenseReportHtml(
                                reportTitle = title,
                                periodLabel = periodLabel,
                                generatedDate = generatedDate,
                                totalIncome = totalIncome,
                                totalExpense = totalExpense,
                                totalSavings = totalSavings,
                                netBalance = netBalance,
                                budgetAmount = budgetAmount,
                                categories = categories,
                                entries = transactions
                            )
                            ReceiptPrintManager.printHtmlDocument(context, html, "Expense_Report_$periodLabel")
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_print_expense_report"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রিন্ট PDF", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val sb = StringBuilder()
                            sb.appendLine("📊 *$title*")
                            sb.appendLine("সময়কাল: $periodLabel ($generatedDate)")
                            sb.appendLine("--------------------------")
                            sb.appendLine("🟢 মোট আয়: ${BengaliUtils.formatTaka(totalIncome)}")
                            sb.appendLine("🔴 মোট খরচ: ${BengaliUtils.formatTaka(totalExpense)}")
                            sb.appendLine("🟡 মোট সঞ্চয়: ${BengaliUtils.formatTaka(totalSavings)}")
                            sb.appendLine("💰 অবশিষ্ট নিট ব্যালেন্স: ${BengaliUtils.formatTaka(netBalance)}")
                            if (budgetAmount > 0) {
                                sb.appendLine("🎯 বাজেট লক্ষ্য: ${BengaliUtils.formatTaka(budgetAmount)}")
                            }
                            if (categories.isNotEmpty()) {
                                sb.appendLine("\n📌 *শীর্ষ খরচের খাত:*")
                                categories.take(5).forEach {
                                    sb.appendLine("• ${it.first}: ${BengaliUtils.formatTaka(it.second)}")
                                }
                            }
                            sb.appendLine("\n— মাওয়া স্মার্ট খাতা")

                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, sb.toString())
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "খরচের রিপোর্ট শেয়ার করুন"))
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_share_expense_report"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(
                        onClick = {
                            val sb = StringBuilder()
                            sb.appendLine("$title ($periodLabel)")
                            sb.appendLine("আয়: ${BengaliUtils.formatTaka(totalIncome)} | খরচ: ${BengaliUtils.formatTaka(totalExpense)} | সঞ্চয়: ${BengaliUtils.formatTaka(totalSavings)}")
                            sb.appendLine("অবশিষ্ট: ${BengaliUtils.formatTaka(netBalance)}")
                            val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            cm.setPrimaryClip(ClipData.newPlainText("Expense Report", sb.toString()))
                            Toast.makeText(context, "রিপোর্ট কপি করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = "কপি করুন", modifier = Modifier.size(18.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportMetricCard(
    title: String,
    amount: Double,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.08f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = BengaliUtils.formatTaka(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

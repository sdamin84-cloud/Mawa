package com.example.mawa.ui.components

import android.widget.Toast
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.ReceiptPrintManager
import com.example.mawa.util.ReportExportUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

/**
 * Customer Detailed Baki Statement and Risk Assessment Report Dialog.
 * Provides complete transaction timeline, risk categorization, aging profile,
 * and 1-tap Print / WhatsApp / Share options.
 */
@Composable
fun CustomerStatementReportDialog(
    customerWithBalance: CustomerWithBalance,
    transactions: List<TransactionEntity>,
    shopName: String,
    shopOwner: String,
    shopPhone: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val customer = customerWithBalance.customer
    val now = System.currentTimeMillis()
    var selectedTab by remember { mutableStateOf("ALL") } // ALL, BAKI, JOMA

    // Risk and Due Aging Calculation
    val fifteenDaysMillis = 15L * 24 * 60 * 60 * 1000
    val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000
    val lastTxTs = customerWithBalance.lastTransaction?.timestamp ?: customer.createdAt
    val daysSinceLastActivity = ((now - lastTxTs) / (24 * 60 * 60 * 1000)).coerceAtLeast(0)

    val isOverLimit = customer.creditLimit > 0 && customerWithBalance.currentBalance > customer.creditLimit
    val isOverduePromise = customer.promisedPaymentDate in 1 until now

    // Health Score (0-100)
    val healthScore = remember(customerWithBalance, daysSinceLastActivity) {
        var score = 100
        if (customerWithBalance.currentBalance > 0) {
            if (daysSinceLastActivity > 30) score -= 35
            else if (daysSinceLastActivity > 15) score -= 15

            if (isOverLimit) score -= 25
            if (isOverduePromise) score -= 20
        }
        score.coerceIn(10, 100)
    }

    val riskLevel = when {
        customerWithBalance.currentBalance <= 0 -> "পরিশোধিত (নিরাপদ)"
        healthScore >= 75 -> "কম ঝুঁকিপূর্ণ (সবুজ)"
        healthScore >= 50 -> "মাঝারি ঝুঁকি (হলুদ)"
        else -> "উচ্চ ঝুঁকিপূর্ণ (লাল)"
    }

    val riskColor = when {
        customerWithBalance.currentBalance <= 0 -> FinancialPositive
        healthScore >= 75 -> FinancialPositive
        healthScore >= 50 -> Color(0xFFD97706)
        else -> FinancialNegative
    }

    val filteredList = remember(transactions, selectedTab) {
        val list = when (selectedTab) {
            "BAKI" -> transactions.filter { it.type == TransactionType.SALE_BAKI }
            "JOMA" -> transactions.filter { it.type == TransactionType.BAKI_COLLECTION }
            else -> transactions
        }
        list.sortedByDescending { it.timestamp }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .testTag("customer_statement_report_dialog"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MawaPrimary.copy(alpha = 0.08f))
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${customer.name} - খতিয়ান রিপোর্ট",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (customer.phone.isNotBlank()) "মোবাইল: ${customer.phone}" else "ঠিকানা: ${customer.address.ifBlank { "নির্দিষ্ট নেই" }}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                        .heightIn(max = 480.dp)
                        .padding(16.dp)
                ) {
                    // 1. KPI Cards Grid
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Current Due
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = FinancialNegative.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FinancialNegative.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("বর্তমান বকেয়া", style = MaterialTheme.typography.labelSmall, color = FinancialNegative)
                                    Text(
                                        text = BengaliUtils.formatTaka(customerWithBalance.currentBalance),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialNegative
                                    )
                                }
                            }

                            // Total Collected
                            Surface(
                                modifier = Modifier.weight(1f),
                                color = FinancialPositive.copy(alpha = 0.08f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.2f))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text("মোট জমা আদায়", style = MaterialTheme.typography.labelSmall, color = FinancialPositive)
                                    Text(
                                        text = BengaliUtils.formatTaka(customerWithBalance.totalJomaReceived),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialPositive
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // 2. Risk & Reliability Analysis Card
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = riskColor.copy(alpha = 0.06f)),
                            border = BorderStroke(1.dp, riskColor.copy(alpha = 0.3f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .size(10.dp)
                                                .clip(CircleShape)
                                                .background(riskColor)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "ঝুঁকি স্তর: $riskLevel",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = riskColor
                                        )
                                    }

                                    Text(
                                        text = "স্কোর: ${BengaliUtils.toBanglaDigits(healthScore.toLong())}/১০০",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = riskColor
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = buildString {
                                        append("• সর্বশেষ লেনদেন: ${BengaliUtils.toBanglaDigits(daysSinceLastActivity)} দিন পূর্বে\n")
                                        if (customer.creditLimit > 0) {
                                            append("• বাকির সীমা: ${BengaliUtils.formatTaka(customer.creditLimit)} ${if (isOverLimit) " (⚠️ সীমা পার হয়েছে)" else " (স্বাভাবিক)"}\n")
                                        }
                                        if (customer.promisedPaymentDate > 0) {
                                            append("• প্রতিশ্রুত তারিখ: ${BengaliUtils.formatTransactionDateOnly(customer.promisedPaymentDate)}${if (isOverduePromise) " (⚠️ অতিক্রম করেছে)" else ""}\n")
                                        }
                                        append("• লেনদেন সংখ্যা: মোট ${BengaliUtils.toBanglaDigits(transactions.size.toLong())} টি")
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 3. Statement Filter Tabs
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "ALL" to "সকল লেনদেন (${BengaliUtils.toBanglaDigits(transactions.size.toLong())})",
                                "BAKI" to "শুধু বাকি (${BengaliUtils.toBanglaDigits(transactions.count { it.type == TransactionType.SALE_BAKI }.toLong())})",
                                "JOMA" to "শুধু জমা (${BengaliUtils.toBanglaDigits(transactions.count { it.type == TransactionType.BAKI_COLLECTION }.toLong())})"
                            ).forEach { (tabKey, label) ->
                                val isSelected = selectedTab == tabKey
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedTab = tabKey },
                                    color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 2.dp),
                                        fontSize = 10.sp,
                                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // 4. Transaction List Rows
                    if (filteredList.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 20.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "কোনো লেনদেন রেকর্ড পাওয়া যায়নি",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        items(filteredList) { tx ->
                            val isBaki = tx.type == TransactionType.SALE_BAKI
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isBaki) "বাকি প্রদান" else "জমা গ্রহণ",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isBaki) FinancialNegative else FinancialPositive
                                    )
                                    if (tx.note.isNotBlank()) {
                                        Text(
                                            text = tx.note,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                    }
                                    Text(
                                        text = BengaliUtils.formatTransactionTime(tx.timestamp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }

                                Text(
                                    text = "${if (isBaki) "+" else "-"}${BengaliUtils.formatTaka(tx.amount)}",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBaki) FinancialNegative else FinancialPositive
                                )
                            }
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                                thickness = 0.5.dp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Bottom Action Buttons: Print PDF | Image Memo | Share Text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Print Statement PDF
                    OutlinedButton(
                        onClick = {
                            val html = ReceiptPrintManager.generateCustomerFullLedgerHtml(
                                shopName = shopName,
                                shopOwner = shopOwner,
                                shopPhone = shopPhone,
                                customerWithBalance = customerWithBalance,
                                transactions = transactions,
                                dateGenerated = BengaliUtils.formatTransactionTime(System.currentTimeMillis())
                            )
                            ReceiptPrintManager.printHtmlDocument(context, html, "Ledger_${customer.name}")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("প্রিন্ট PDF", fontSize = 11.sp)
                    }

                    // Share Image Memo
                    OutlinedButton(
                        onClick = {
                            val bitmap = DataBackupRestoreManager.createCustomerBakiMemoBitmap(
                                shopName = shopName,
                                shopPhone = shopPhone,
                                customerWithBalance = customerWithBalance,
                                recentTransactions = transactions
                            )
                            DataBackupRestoreManager.shareBitmapAsImage(
                                context = context,
                                bitmap = bitmap,
                                fileNamePrefix = "memo_${customer.name.replace(" ", "_")}",
                                title = "${customer.name} - বাকি মেমো"
                            )
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ছবি মেমো", fontSize = 11.sp)
                    }

                    // Share WhatsApp / SMS
                    OutlinedButton(
                        onClick = {
                            val text = buildString {
                                append("জনাব ${customer.name},\n")
                                append("$shopName-এ আপনার খতিয়ান হিসাব:\n")
                                append("মোট বাকি নেওয়া: ${BengaliUtils.formatTaka(customerWithBalance.totalBakiGiven)}\n")
                                append("মোট জমা দেওয়া: ${BengaliUtils.formatTaka(customerWithBalance.totalJomaReceived)}\n")
                                append("বর্তমান মোট বকেয়া: ${BengaliUtils.formatTaka(customerWithBalance.currentBalance)}\n\n")
                                append("ধন্যবাদ,\n$shopName")
                                if (shopPhone.isNotBlank()) append("\nমোবাইল: $shopPhone")
                            }
                            ReportExportUtils.shareText(context, text, "${customer.name}-এর বাকি খতিয়ান")
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার", fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

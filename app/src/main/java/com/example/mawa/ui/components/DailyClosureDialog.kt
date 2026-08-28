package com.example.mawa.ui.components

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DailyClosureManager
import com.example.mawa.util.DailyClosureRecord
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary

@Composable
fun DailyClosureDialog(
    dateMillis: Long = System.currentTimeMillis(),
    openingBalance: Double = 0.0,
    cashSales: Double = 0.0,
    bakiSales: Double = 0.0,
    bakiCollection: Double = 0.0,
    shopExpenses: Double = 0.0,
    homeWithdrawals: Double = 0.0,
    purchases: Double = 0.0,
    onDismiss: () -> Unit,
    onClosureSaved: () -> Unit = {}
) {
    val context = LocalContext.current
    val dateKey = remember(dateMillis) { DailyClosureManager.getDateKey(dateMillis) }
    val existingRecord = remember(dateKey) { DailyClosureManager.getClosureRecord(context, dateKey) }

    val isAlreadyLocked = existingRecord?.isLocked == true

    val totalInflow = openingBalance + cashSales + bakiCollection
    val totalOutflow = purchases + shopExpenses + homeWithdrawals
    val calculatedExpectedCash = totalInflow - totalOutflow

    var physicalCashInput by remember {
        mutableStateOf(
            if (existingRecord != null && existingRecord.actualPhysicalCash > 0)
                existingRecord.actualPhysicalCash.toString()
            else calculatedExpectedCash.toString()
        )
    }

    var closureNote by remember { mutableStateOf(existingRecord?.note ?: "") }

    val physicalCashNum = physicalCashInput.toDoubleOrNull() ?: 0.0
    val discrepancy = physicalCashNum - calculatedExpectedCash

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(6.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isAlreadyLocked) FinancialPositiveContainer else MawaPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isAlreadyLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    tint = if (isAlreadyLocked) FinancialPositive else MawaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isAlreadyLocked) "দিন শেষের হিসাব লকড" else "দিন ক্লোজ ও হিসাব লক",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = BengaliUtils.getFormattedTodayDate(dateMillis),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary Table Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("প্রারম্ভিক ক্যাশ (সাবেক):", style = MaterialTheme.typography.bodySmall)
                            Text(BengaliUtils.formatTaka(openingBalance), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("মোট নগদ প্রাপ্তি (বিক্রি + আদায়):", style = MaterialTheme.typography.bodySmall, color = FinancialPositive)
                            Text("+${BengaliUtils.formatTaka(cashSales + bakiCollection)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = FinancialPositive)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("মোট নগদ খরচ ও ক্রয়:", style = MaterialTheme.typography.bodySmall, color = FinancialNegative)
                            Text("-${BengaliUtils.formatTaka(totalOutflow)}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = FinancialNegative)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("হিসাব অনুযায়ী প্রত্যাশিত ক্যাশ:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text(BengaliUtils.formatTaka(calculatedExpectedCash), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MawaPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Physical Cash Input
                MawaAmountInput(
                    amount = physicalCashInput,
                    onAmountChange = { physicalCashInput = it },
                    label = "হাতে থাকা গণনা করা ক্যাশ (Physical Cash)",
                    modifier = Modifier.fillMaxWidth().testTag("input_physical_cash_closing")
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Discrepancy Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        discrepancy == 0.0 -> FinancialPositiveContainer
                        discrepancy < 0 -> FinancialNegativeContainer
                        else -> FinancialWarningContainer
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when {
                                discrepancy == 0.0 -> Icons.Default.CheckCircle
                                else -> Icons.Default.Warning
                            },
                            contentDescription = null,
                            tint = when {
                                discrepancy == 0.0 -> FinancialPositive
                                discrepancy < 0 -> FinancialNegative
                                else -> FinancialWarning
                            },
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when {
                                discrepancy == 0.0 -> "✅ ক্যাশ ও হিসাব পুরোপুরি মিলেছে!"
                                discrepancy < 0 -> "⚠️ ক্যাশ ঘাটতি: ${BengaliUtils.formatTaka(-discrepancy)} কম আছে"
                                else -> "ℹ️ ক্যাশ উদ্বৃত্ত: ${BengaliUtils.formatTaka(discrepancy)} বেশি আছে"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                discrepancy == 0.0 -> FinancialPositive
                                discrepancy < 0 -> FinancialNegative
                                else -> FinancialWarning
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Optional Note
                OutlinedTextField(
                    value = closureNote,
                    onValueChange = { closureNote = it },
                    label = { Text("ক্লোজিং নোট / মন্তব্য (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ক্যাশ ড্রয়ার চেক করা হয়েছে") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MawaPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Actions: Lock or Unlock
                if (isAlreadyLocked) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                DailyClosureManager.unlockDay(context, dateKey)
                                Toast.makeText(context, "হিসাব আনলক করা হয়েছে", Toast.LENGTH_SHORT).show()
                                onClosureSaved()
                                onDismiss()
                            },
                            modifier = Modifier.weight(1f).height(44.dp).testTag("btn_unlock_day"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("আনলক করুন", fontSize = 12.sp)
                        }

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f).height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("বন্ধ করুন", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            val record = DailyClosureRecord(
                                dateKey = dateKey,
                                closedTimestamp = System.currentTimeMillis(),
                                openingBalance = openingBalance,
                                totalCashSales = cashSales,
                                totalBakiSales = bakiSales,
                                totalBakiCollection = bakiCollection,
                                totalExpenses = shopExpenses + homeWithdrawals,
                                totalPurchases = purchases,
                                expectedCashInHand = calculatedExpectedCash,
                                actualPhysicalCash = physicalCashNum,
                                discrepancy = discrepancy,
                                note = closureNote,
                                isLocked = true
                            )
                            DailyClosureManager.saveClosureRecord(context, record)
                            Toast.makeText(context, "🔒 আজকের দিন সফলভাবে ক্লোজ ও হিসাব লক করা হয়েছে!", Toast.LENGTH_LONG).show()
                            onClosureSaved()
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth().height(46.dp).testTag("btn_confirm_close_and_lock_day"),
                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "হিসাব চূড়ান্ত ও দিন লক করুন", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

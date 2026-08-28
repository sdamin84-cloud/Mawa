package com.example.mawa.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

@Composable
fun SettlementDiscountDialog(
    customerWithBalance: CustomerWithBalance,
    onDismiss: () -> Unit,
    onSettle: (cashPaid: Double, discountGiven: Double, note: String) -> Unit
) {
    val cust = customerWithBalance.customer
    val currentDue = customerWithBalance.currentBalance

    var cashPaidInput by remember { mutableStateOf(currentDue.toInt().toString()) }
    var discountInput by remember { mutableStateOf("0") }
    var note by remember { mutableStateOf("পূর্ণ হিসাব নিষ্পত্তি ও ছাড়") }

    val cashPaid = cashPaidInput.toDoubleOrNull() ?: 0.0
    val discount = discountInput.toDoubleOrNull() ?: 0.0
    val totalSettled = cashPaid + discount
    val remainingAfter = currentDue - totalSettled

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Handshake,
                    contentDescription = null,
                    tint = FinancialPositive,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "খাতা নিষ্পত্তি ও ছাড় (Settlement)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "গ্রাহক: ${cust.name}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "বর্তমান বকেয়া বাকি: ${BengaliUtils.formatTaka(currentDue)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = FinancialNegative,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = cashPaidInput,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            cashPaidInput = it
                            val paid = it.toDoubleOrNull() ?: 0.0
                            val disc = (currentDue - paid).coerceAtLeast(0.0)
                            discountInput = disc.toInt().toString()
                        }
                    },
                    label = { Text("নগদ প্রাপ্তি (টাকা পরিশোধ)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = discountInput,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            discountInput = it
                        }
                    },
                    label = { Text("ছাড় / কমিশন (ডিসকাউন্ট)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("মন্তব্য") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "নিষ্পত্তির পর বাকি থাকবে: ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = BengaliUtils.formatTaka(remainingAfter.coerceAtLeast(0.0)),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (remainingAfter <= 0.0) FinancialPositive else FinancialNegative
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (totalSettled > 0) {
                        onSettle(cashPaid, discount, note)
                    }
                },
                enabled = totalSettled > 0,
                colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
            ) {
                Text("নিষ্পত্তি সম্পন্ন করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

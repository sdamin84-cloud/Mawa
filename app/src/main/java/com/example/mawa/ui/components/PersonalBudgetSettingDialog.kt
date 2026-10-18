package com.example.mawa.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.MawaPrimary

@Composable
fun PersonalBudgetSettingDialog(
    currentBudget: Double,
    title: String = "মাসিক বাজেট নির্ধারণ",
    onDismiss: () -> Unit,
    onSaveBudget: (Double) -> Unit
) {
    var budgetInput by remember {
        mutableStateOf(if (currentBudget > 0) currentBudget.toInt().toString() else "")
    }

    val quickPresets = listOf(10000, 15000, 20000, 25000, 30000, 50000)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = title, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "মাসিক খরচের একটি সীমা বা বাজেট নির্ধারণ করলে আপনি প্রতিদিনের খরচের গতি এবং সাশ্রয় ট্র্যাক করতে পারবেন।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = {
                        if (it.isEmpty() || it.all { ch -> ch.isDigit() }) {
                            budgetInput = it
                        }
                    },
                    label = { Text("মাসিক বাজেট লক্ষ্য (৳)") },
                    placeholder = { Text("যেমন: 20000") },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_budget_amount")
                )

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "দ্রুত বাজেট বাছাই:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(quickPresets) { preset ->
                        val isSelected = budgetInput == preset.toString()
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { budgetInput = preset.toString() },
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "৳${BengaliUtils.toBanglaDigits(preset.toLong())}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = budgetInput.toDoubleOrNull() ?: 0.0
                    onSaveBudget(amount)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                modifier = Modifier.testTag("btn_save_budget")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            Row {
                if (currentBudget > 0) {
                    TextButton(
                        onClick = {
                            onSaveBudget(0.0)
                            onDismiss()
                        }
                    ) {
                        Text("বাজেট মুছুন", color = MaterialTheme.colorScheme.error)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("বাতিল")
                }
            }
        }
    )
}

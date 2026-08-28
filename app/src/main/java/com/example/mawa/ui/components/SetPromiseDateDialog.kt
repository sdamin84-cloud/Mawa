package com.example.mawa.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Event
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.MawaPrimary
import java.util.Calendar

@Composable
fun SetPromiseDateDialog(
    customer: CustomerEntity,
    onDismiss: () -> Unit,
    onDateSet: (Long) -> Unit
) {
    val now = System.currentTimeMillis()
    var selectedTimestamp by remember {
        mutableStateOf(if (customer.promisedPaymentDate > 0) customer.promisedPaymentDate else (now + 7L * 24 * 60 * 60 * 1000))
    }

    val options = listOf(
        "৩ দিন পর" to (now + 3L * 24 * 60 * 60 * 1000),
        "৭ দিন পর" to (now + 7L * 24 * 60 * 60 * 1000),
        "১৫ দিন পর" to (now + 15L * 24 * 60 * 60 * 1000),
        "১ মাস পর" to (now + 30L * 24 * 60 * 60 * 1000)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Event,
                    contentDescription = null,
                    tint = MawaPrimary,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "টাকা দেওয়ার প্রতিশ্রুত তারিখ",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "${customer.name}-এর জন্য টাকা পরিশোধের সম্ভাব্য তারিখ নির্ধারণ করুন:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(14.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    options.forEach { (label, timestamp) ->
                        val isSelected = kotlin.math.abs(selectedTimestamp - timestamp) < 12 * 60 * 60 * 1000
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { selectedTimestamp = timestamp },
                            color = if (isSelected) MawaPrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = BengaliUtils.formatTransactionDateOnly(timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                if (customer.promisedPaymentDate > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { onDateSet(0L) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = FinancialNegative),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("প্রতিশ্রুত তারিখ বাতিল / মুছে ফেলুন")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onDateSet(selectedTimestamp) },
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary)
            ) {
                Text("নিশ্চিত করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

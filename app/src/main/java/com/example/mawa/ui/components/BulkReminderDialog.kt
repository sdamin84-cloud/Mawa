package com.example.mawa.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.window.DialogProperties
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

enum class BulkFilter(val title: String) {
    ALL_DUE("সব বাকিদার"),
    OLD_30_DAYS("৩০+ দিন পুরানো"),
    OVERDUE_PROMISE("তারিখ পার হয়েছে")
}

@Composable
fun BulkReminderDialog(
    customersWithBalance: List<CustomerWithBalance>,
    shopName: String,
    shopPhone: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("mawa_reminder_prefs", Context.MODE_PRIVATE) }
    var paymentNumber by remember {
        mutableStateOf(prefs.getString("saved_payment_number", shopPhone) ?: shopPhone)
    }

    var selectedTone by remember { mutableStateOf(ReminderTone.POLITE) }
    var selectedBulkFilter by remember { mutableStateOf(BulkFilter.ALL_DUE) }
    val sentStatusMap = remember { mutableStateMapOf<Long, Boolean>() }

    val now = System.currentTimeMillis()
    val thirtyDaysMillis = 30L * 24 * 60 * 60 * 1000

    val dueCustomers = remember(customersWithBalance, selectedBulkFilter) {
        customersWithBalance.filter { it.currentBalance > 0 }.filter { item ->
            when (selectedBulkFilter) {
                BulkFilter.ALL_DUE -> true
                BulkFilter.OLD_30_DAYS -> {
                    val lastTs = item.lastTransaction?.timestamp ?: item.customer.createdAt
                    (now - lastTs) >= thirtyDaysMillis
                }
                BulkFilter.OVERDUE_PROMISE -> {
                    item.customer.promisedPaymentDate > 0 && item.customer.promisedPaymentDate < now
                }
            }
        }.sortedByDescending { it.currentBalance }
    }

    fun buildMessageForCustomer(custName: String, dueBalance: Double): String {
        val dueFormatted = BengaliUtils.formatTaka(dueBalance)
        val payInfo = if (paymentNumber.isNotBlank()) "\nবিকাশ/নগদ নম্বর: $paymentNumber" else ""

        return when (selectedTone) {
            ReminderTone.POLITE ->
                "আসসালামু আলাইকুম $custName ভাই/ম্যাম,\n" +
                "$shopName থেকে বিনীতভাবে জানাচ্ছি যে আপনার বর্তমান বকেয়া হিসাব $dueFormatted।\n" +
                "সুবিধাজনক সময়ে পরিশোধের বিনীত অনুরোধ রইল।$payInfo\nধন্যবাদ।"

            ReminderTone.REGULAR ->
                "সম্মানিত গ্রাহক $custName,\n" +
                "$shopName-এ আপনার বিগত কেনাকাটার অবশিষ্ট বকেয়া $dueFormatted।\n" +
                "হিসাবটি মিলিয়ে পরিশোধ করার অনুরোধ করা হচ্ছে।$payInfo\nধন্যবাদ।"

            ReminderTone.URGENT ->
                "জরুরি তাগিদবার্তা!\n" +
                "শ্রদ্ধেয় $custName, $shopName-এ আপনার বকেয়া $dueFormatted অতি দ্রুত পরিশোধের জন্য বিশেষ অনুরোধ জানানো হচ্ছে।$payInfo\n" +
                "যোগাযোগ: $shopPhone"

            ReminderTone.PAYMENT_QR ->
                "আসসালামু আলাইকুম $custName ভাই,\n" +
                "$shopName হতে আপনার মোট বাকি $dueFormatted।\n" +
                "আপনি সরাসরি দোকানে এসে অথবা বিকাশ/নগদে পরিশোধ করতে পারেন:\n" +
                "📱 পেমেন্ট নম্বর: $paymentNumber\n" +
                "টাকা পাঠানোর পর দয়া করে জানাবেন। ধন্যবাদ।"
        }
    }

    fun sendSms(phone: String, message: String, custId: Long) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${phone.trim()}")
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            sentStatusMap[custId] = true
        } catch (e: Exception) {
            Toast.makeText(context, "SMS অ্যাপ খোলা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
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
                        Surface(
                            shape = CircleShape,
                            color = MawaPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = MawaPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "তাগাদা বার্তা হাব (Bulk Reminder)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "বাকিদার: ${BengaliUtils.toBanglaDigits(dueCustomers.size.toLong())} জন · মোট বাকি ${BengaliUtils.formatTaka(dueCustomers.sumOf { it.currentBalance })}",
                                style = MaterialTheme.typography.bodySmall,
                                color = FinancialNegative,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tone Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ReminderTone.values().forEach { tone ->
                        val isSelected = selectedTone == tone
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedTone = tone },
                            color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, if (isSelected) MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tone.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                                fontSize = 10.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Filter Tabs (সব বাকিদার / ৩০+ দিন / তারিখ পার)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BulkFilter.values().forEach { filter ->
                        val isSelected = selectedBulkFilter == filter
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { selectedBulkFilter = filter },
                            color = if (isSelected) FinancialNegative.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = BorderStroke(1.dp, if (isSelected) FinancialNegative else Color.Transparent),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text(
                                text = filter.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) FinancialNegative else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Payment number input
                OutlinedTextField(
                    value = paymentNumber,
                    onValueChange = {
                        paymentNumber = it
                        prefs.edit().putString("saved_payment_number", it).apply()
                    },
                    label = { Text("বিকাশ/নগদ নম্বর (ঐচ্ছিক)") },
                    placeholder = { Text("017XXXXXXXX") },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MawaPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Customer List
                if (dueCustomers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "এই ফিল্টারে কোনো বাকিদার পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dueCustomers, key = { it.customer.id }) { item ->
                            val cust = item.customer
                            val isSent = sentStatusMap[cust.id] == true
                            val msg = buildMessageForCustomer(cust.name, item.currentBalance)

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSent) FinancialPositive.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSent) FinancialPositive.copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = cust.name,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            if (isSent) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = "পাঠানো হয়েছে",
                                                    tint = FinancialPositive,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = if (cust.phone.isNotBlank()) cust.phone else "নম্বর নেই",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 11.sp
                                        )
                                        Text(
                                            text = "বাকি: ${BengaliUtils.formatTaka(item.currentBalance)}",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = FinancialNegative,
                                            fontSize = 12.sp
                                        )
                                    }

                                    if (cust.phone.isNotBlank()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            // WhatsApp Action
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        DataBackupRestoreManager.sendWhatsAppMessage(
                                                            context = context,
                                                            phone = cust.phone,
                                                            message = msg
                                                        )
                                                        sentStatusMap[cust.id] = true
                                                    },
                                                color = FinancialPositive,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Send,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "WhatsApp",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }

                                            // SMS Action
                                            Surface(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable {
                                                        sendSms(cust.phone, msg, cust.id)
                                                    },
                                                color = MawaPrimary,
                                                shape = RoundedCornerShape(8.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Message,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(13.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(3.dp))
                                                    Text(
                                                        text = "SMS",
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    )
                                                }
                                            }
                                        }
                                    } else {
                                        Text(
                                            text = "ফোন নম্বর নেই",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontSize = 10.sp
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
}

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

enum class ReminderTone(val title: String, val badge: String) {
    POLITE("নম্র তাগাদা", "অনুরোধমূলক"),
    REGULAR("বকেয়া বিবরণী", "সাধারণ নোটিশ"),
    URGENT("জরুরি তাগাদা", "তাগিদবার্তা"),
    PAYMENT_QR("বিকাশ/নগদসহ", "পেমেন্ট লিংক")
}

@Composable
fun BakiReminderDialog(
    customerWithBalance: CustomerWithBalance,
    shopName: String,
    shopPhone: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val prefs = remember { context.getSharedPreferences("mawa_reminder_prefs", Context.MODE_PRIVATE) }
    var paymentNumber by remember {
        mutableStateOf(prefs.getString("saved_payment_number", shopPhone) ?: shopPhone)
    }

    var selectedTone by remember { mutableStateOf(ReminderTone.POLITE) }

    val customer = customerWithBalance.customer
    val dueFormatted = BengaliUtils.formatTaka(customerWithBalance.currentBalance)

    // Generate message text based on selected tone
    val reminderMessage = remember(selectedTone, shopName, customer.name, dueFormatted, paymentNumber) {
        val payInfo = if (paymentNumber.isNotBlank()) "\nবিকাশ/নগদ নম্বর: $paymentNumber" else ""

        when (selectedTone) {
            ReminderTone.POLITE ->
                "আসসালামু আলাইকুম ${customer.name} ভাই/ম্যাম,\n" +
                "$shopName থেকে বিনীতভাবে জানাচ্ছি যে আপনার বর্তমান বকেয়া হিসাব $dueFormatted।\n" +
                "সুবিধাজনক সময়ে পরিশোধের বিনীত অনুরোধ রইল।$payInfo\nধন্যবাদ।"

            ReminderTone.REGULAR ->
                "সম্মানিত গ্রাহক ${customer.name},\n" +
                "$shopName-এ আপনার বিগত কেনাকাটার অবশিষ্ট বকেয়া $dueFormatted।\n" +
                "হিসাবটি মিলিয়ে পরিশোধ করার অনুরোধ করা হচ্ছে।$payInfo\nধন্যবাদ।"

            ReminderTone.URGENT ->
                "জরুরি তাগিদবার্তা!\n" +
                "শ্রদ্ধেয় ${customer.name}, $shopName-এ আপনার দীর্ঘদিনের বকেয়া $dueFormatted অতি দ্রুত পরিশোধের জন্য বিশেষ অনুরোধ জানানো হচ্ছে।$payInfo\n" +
                "যোগাযোগ: $shopPhone"

            ReminderTone.PAYMENT_QR ->
                "আসসালামু আলাইকুম ${customer.name} ভাই,\n" +
                "$shopName হতে আপনার মোট বাকি $dueFormatted।\n" +
                "আপনি সরাসরি দোকানে এসে অথবা বিকাশ/নগদে পরিশোধ করতে পারেন:\n" +
                "📱 পেমেন্ট নম্বর: $paymentNumber (Personal/Merchant)\n" +
                "টাকা পাঠানোর পর দয়া করে জানাবেন। ধন্যবাদ।"
        }
    }

    fun savePaymentNumber(num: String) {
        paymentNumber = num
        prefs.edit().putString("saved_payment_number", num).apply()
    }

    fun sendViaSms() {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:${customer.phone.trim()}")
                putExtra("sms_body", reminderMessage)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "SMS অ্যাপ চালু করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
        }
    }

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
                            color = FinancialPositive.copy(alpha = 0.15f),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = FinancialPositive,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "বাকি তাগাদা বার্তা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${customer.name} · বাকি $dueFormatted",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = FinancialNegative
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tone Selector Pills
                Text(
                    text = "বার্তার ধরন নির্বাচন করুন:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))

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
                                .clickable { selectedTone = tone }
                                .testTag("reminder_tone_${tone.name.lowercase()}"),
                            color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = tone.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                fontSize = 11.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Payment Number Input Field
                OutlinedTextField(
                    value = paymentNumber,
                    onValueChange = { savePaymentNumber(it) },
                    label = { Text("বিকাশ / নগদ নম্বর (মেসেজে যুক্ত হবে)") },
                    placeholder = { Text("017XXXXXXXX") },
                    leadingIcon = { Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(18.dp)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MawaPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Live Preview Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "বার্তা প্রিভিউ:",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MawaPrimary
                            )
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(reminderMessage))
                                    Toast.makeText(context, "মেসেজ কপি হয়েছে", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "কপি", modifier = Modifier.size(16.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = reminderMessage,
                            style = MaterialTheme.typography.bodySmall,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: [ WhatsApp ] [ SMS ] [ Copy ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp Button
                    Button(
                        onClick = {
                            DataBackupRestoreManager.sendWhatsAppMessage(
                                context = context,
                                phone = customer.phone,
                                message = reminderMessage
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_send_whatsapp_reminder"),
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("হোয়াটসঅ্যাপ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    // SMS Button
                    Button(
                        onClick = { sendViaSms() },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_send_sms_reminder"),
                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("সরাসরি SMS", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

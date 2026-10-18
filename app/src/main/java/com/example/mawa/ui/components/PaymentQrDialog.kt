package com.example.mawa.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.QrCodeGenerator
import com.example.mawa.util.QrCodeImage
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

/**
 * PaymentQrDialog:
 * Complete Digital Counter Payment Hub for shopkeepers.
 * Features:
 * - High-res instant QR code for customer scan-to-pay
 * - Dynamic custom amount entering (e.g. customer owes ৳650)
 * - Copyable bKash & Nagad numbers
 * - WhatsApp & SMS direct sharing with formatted payment instructions
 * - Direct shortcut to edit payment accounts in ShopProfile
 */
@Composable
fun PaymentQrDialog(
    settings: ShopSettingsEntity?,
    initialAmount: Double = 0.0,
    customerName: String = "",
    onDismiss: () -> Unit,
    onEditShopProfile: () -> Unit
) {
    val context = LocalContext.current
    var amountInput by remember {
        mutableStateOf(if (initialAmount > 0) (if (initialAmount % 1.0 == 0.0) initialAmount.toLong().toString() else initialAmount.toString()) else "")
    }

    val shopName = settings?.shopName?.takeIf { it.isNotBlank() && it != "মাওয়া ডিজিটাল খাতা" } ?: "আমার দোকান"
    val shopPhone = settings?.phone ?: ""
    val bkashNum = settings?.bkashNumber ?: ""
    val nagadNum = settings?.nagadNumber ?: ""
    val bankDetails = settings?.bankDetails ?: ""

    val parsedAmount = amountInput.toDoubleOrNull() ?: 0.0

    // Build QR Content Payload
    val qrPayload = remember(shopName, shopPhone, bkashNum, nagadNum, bankDetails, parsedAmount) {
        QrCodeGenerator.buildPaymentQrPayload(
            shopName = shopName,
            phone = shopPhone,
            bkashNumber = bkashNum,
            nagadNumber = nagadNum,
            bankDetails = bankDetails,
            amount = parsedAmount
        )
    }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "$label কপি হয়েছে: $text", Toast.LENGTH_SHORT).show()
    }

    fun sharePaymentDetails() {
        val amountStr = if (parsedAmount > 0) "\nপরিশোধযোগ্য টাকা: ${BengaliUtils.formatTaka(parsedAmount)}" else ""
        val custStr = if (customerName.isNotBlank()) "সম্মানিত গ্রাহক $customerName,\n" else ""
        val text = buildString {
            append("$custStr$shopName-এ ডিজিটাল পেমেন্ট করুন:$amountStr\n")
            if (bkashNum.isNotBlank()) append("📱 bKash (বিকাশ): $bkashNum\n")
            if (nagadNum.isNotBlank()) append("📱 Nagad (নগদ): $nagadNum\n")
            if (bankDetails.isNotBlank()) append("🏦 ব্যাংক হিসাব: $bankDetails\n")
            if (shopPhone.isNotBlank()) append("দোকানের মোবাইল: $shopPhone\n")
            append("টাকা পরিশোধের পর রেফারেন্সে আপনার নাম বা মেমো নম্বর লিখুন।\n")
            append("সহযোগিতায়: MAWA ডিজিটাল খাতা")
        }

        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "পেমেন্ট তথ্য শেয়ার করুন")
        context.startActivity(shareIntent)
    }

    fun sendSmsPayment() {
        val amountStr = if (parsedAmount > 0) " টাকার পরিমাণ: ${BengaliUtils.formatTaka(parsedAmount)}।" else ""
        val text = "$shopName ডিজিটাল পেমেন্ট:$amountStr " +
                (if (bkashNum.isNotBlank()) "bKash: $bkashNum, " else "") +
                (if (nagadNum.isNotBlank()) "Nagad: $nagadNum" else "")
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("sms:")
                putExtra("sms_body", text)
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(context, "মেসেজ অ্যাপ খোলা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
        }
    }

    val hasAnyPaymentMethod = bkashNum.isNotBlank() || nagadNum.isNotBlank() || bankDetails.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFE2136E), Color(0xFFF7941D))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode2,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "ডিজিটাল পেমেন্ট কাউন্টার ও QR",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$shopName · bKash / Nagad কালেকশন",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 11.sp
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!hasAnyPaymentMethod) {
                    // Alert that no payment number has been set yet
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "আপনার বিকাশ বা নগদ নম্বর এখনো সেট করা হয়নি!",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "দোকানের বিকাশ ও নগদ নম্বর একবার সেট করলেই গ্রাহকরা মেমো ও কিউআর স্ক্যান করে সরাসরি টাকা দিতে পারবে।",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                            )
                            Button(
                                onClick = {
                                    onDismiss()
                                    onEditShopProfile()
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("নম্বর সেট করুন")
                            }
                        }
                    }
                }

                // Interactive Amount Field (Optional for instant billing)
                OutlinedTextField(
                    value = amountInput,
                    onValueChange = { amountInput = it },
                    label = { Text("টাকার পরিমাণ (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ৫০০") },
                    prefix = { Text("৳ ", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = { Text("টাকা লিখলে কিউআর কোডটিতে স্বয়ংক্রিয়ভাবে উক্ত মূল্য যুক্ত হবে") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_qr_amount"),
                    shape = RoundedCornerShape(10.dp)
                )

                // High-Contrast QR Code Card for Customer Scanning
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFCBD5E1)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = shopName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        if (parsedAmount > 0) {
                            Text(
                                text = "প্রদেয় বিল: ${BengaliUtils.formatTaka(parsedAmount)}",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = Color(0xFF16A34A),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // QR Code View
                        Box(
                            modifier = Modifier
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            QrCodeImage(
                                qrContent = qrPayload,
                                size = 180.dp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "গ্রাহক অ্যাপ দিয়ে স্ক্যান করে পেমেন্ট করুন",
                                fontSize = 11.sp,
                                color = Color(0xFF64748B),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                // Payment Numbers List
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (bkashNum.isNotBlank()) {
                        PaymentNumberRow(
                            brand = "bKash",
                            brandColor = Color(0xFFE2136E),
                            brandBg = Color(0xFFFCE7F3),
                            number = bkashNum,
                            onCopy = { copyToClipboard("bKash নম্বর", bkashNum) }
                        )
                    }

                    if (nagadNum.isNotBlank()) {
                        PaymentNumberRow(
                            brand = "Nagad",
                            brandColor = Color(0xFFF7941D),
                            brandBg = Color(0xFFFFF7ED),
                            number = nagadNum,
                            onCopy = { copyToClipboard("নগদ নম্বর", nagadNum) }
                        )
                    }

                    if (bankDetails.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccountBalance,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Column {
                                        Text(
                                            text = "ব্যাংক হিসাব",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = bankDetails,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                OutlinedButton(
                                    onClick = { copyToClipboard("ব্যাংক হিসাব", bankDetails) },
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text("কপি", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                // Share & WhatsApp Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { sharePaymentDetails() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার", fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { sendSmsPayment() },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("SMS", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onDismiss()
                            onEditShopProfile()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1.2f)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এডিট", fontSize = 12.sp)
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("সম্পন্ন")
            }
        }
    )
}

@Composable
private fun PaymentNumberRow(
    brand: String,
    brandColor: Color,
    brandBg: Color,
    number: String,
    onCopy: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = brandBg,
        border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = brandColor
                ) {
                    Text(
                        text = brand,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = number,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF0F172A)
                )
            }

            OutlinedButton(
                onClick = onCopy,
                shape = RoundedCornerShape(6.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, brandColor.copy(alpha = 0.6f))
            ) {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = brandColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("কপি", color = brandColor, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

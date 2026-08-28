package com.example.mawa.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.InvoiceItem
import com.example.mawa.util.ReceiptPrintManager
import com.example.mawa.util.ReportExportUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DigitalReceiptDialog(
    shopName: String,
    shopPhone: String,
    shopAddress: String = "",
    customerName: String? = null,
    customerPhone: String? = null,
    items: List<InvoiceItem>,
    subtotal: Double,
    discount: Double = 0.0,
    paidAmount: Double,
    previousDue: Double = 0.0,
    currentDue: Double = 0.0,
    note: String = "",
    memoNo: String = "M-${System.currentTimeMillis() % 100000}",
    timestamp: Long = System.currentTimeMillis(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val dateFormatted = remember(timestamp) {
        SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("bn", "BD")).format(Date(timestamp))
    }

    val totalPayable = (subtotal - discount) + previousDue

    // Generated HTML for Print / Save as PDF
    val invoiceHtml = remember {
        ReceiptPrintManager.generateInvoiceHtml(
            shopName = shopName,
            shopPhone = shopPhone,
            shopAddress = shopAddress,
            memoNo = memoNo,
            dateFormatted = dateFormatted,
            customerName = customerName,
            customerPhone = customerPhone,
            items = items,
            subtotal = subtotal,
            discount = discount,
            paidAmount = paidAmount,
            previousDue = previousDue,
            currentDue = currentDue,
            note = note
        )
    }

    // Thermal POS String
    val thermalText = remember {
        ReceiptPrintManager.generateThermalPosString(
            shopName = shopName,
            shopPhone = shopPhone,
            memoNo = memoNo,
            dateFormatted = dateFormatted,
            customerName = customerName,
            items = items,
            totalAmount = totalPayable,
            paidAmount = paidAmount,
            dueAmount = currentDue
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header Action Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MawaPrimary.copy(alpha = 0.15f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MawaPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ডিজিটাল ক্যাশ মেমো",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Realistic Paper Memo Card Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFCF9)),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        // Shop Banner
                        Text(
                            text = shopName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        if (shopPhone.isNotBlank()) {
                            Text(
                                text = "মোবাইল: $shopPhone",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF64748B),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(thickness = 1.dp, color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(6.dp))

                        // Memo Info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "মেমো নং: $memoNo", fontSize = 11.sp, color = Color(0xFF334155), fontWeight = FontWeight.Bold)
                            Text(text = dateFormatted, fontSize = 11.sp, color = Color(0xFF64748B))
                        }

                        // Customer Info
                        if (!customerName.isNullOrBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "গ্রাহক: $customerName ${if (!customerPhone.isNullOrBlank()) "($customerPhone)" else ""}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0284C7)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Table Headers
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF1F5F9))
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "পণ্য / বিবরণ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(1.5f))
                            Text(text = "পরিমাণ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(0.8f))
                            Text(text = "মোট", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), modifier = Modifier.weight(0.9f), textAlign = androidx.compose.ui.text.style.TextAlign.End)
                        }

                        // Items List (Scrollable if many)
                        Column(modifier = Modifier.fillMaxWidth()) {
                            items.forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 4.dp, vertical = 3.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = item.name, fontSize = 11.sp, color = Color(0xFF334155), modifier = Modifier.weight(1.5f))
                                    val qtyStr = if (item.quantity > 0) "${item.quantity} ${item.unit}" else "-"
                                    Text(text = qtyStr, fontSize = 11.sp, color = Color(0xFF64748B), modifier = Modifier.weight(0.8f))
                                    Text(
                                        text = BengaliUtils.formatTaka(item.amount),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A),
                                        modifier = Modifier.weight(0.9f),
                                        textAlign = androidx.compose.ui.text.style.TextAlign.End
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFCBD5E1))
                        Spacer(modifier = Modifier.height(4.dp))

                        // Totals Rows
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("মোট মূল্য:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                            Text(BengaliUtils.formatTaka(subtotal), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }

                        if (previousDue > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("পূর্বের বকেয়া:", fontSize = 11.sp, color = Color(0xFFDC2626))
                                Text(BengaliUtils.formatTaka(previousDue), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            }
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("জমা (নগদ):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                            Text(BengaliUtils.formatTaka(paidAmount), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF16A34A))
                        }

                        if (currentDue > 0) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("অবশিষ্ট বাকি:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                Text(BengaliUtils.formatTaka(currentDue), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons: [ 🖨️ প্রিন্ট / PDF ] [ 💬 হোয়াটসঅ্যাপ ] [ 📤 শেয়ার ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Print / PDF Button
                    Button(
                        onClick = {
                            ReceiptPrintManager.printHtml(
                                context = context,
                                htmlContent = invoiceHtml,
                                jobName = "Mawa_Memo_$memoNo"
                            )
                        },
                        modifier = Modifier
                            .weight(1.2f)
                            .height(44.dp)
                            .testTag("btn_print_receipt"),
                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("প্রিন্ট / PDF", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // WhatsApp Share Button
                    Button(
                        onClick = {
                            DataBackupRestoreManager.sendWhatsAppMessage(
                                context = context,
                                phone = customerPhone ?: "",
                                message = thermalText
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("btn_whatsapp_receipt"),
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("হোয়াটসঅ্যাপ", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }

                    // Copy Thermal text
                    Surface(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        IconButton(onClick = {
                            clipboardManager.setText(AnnotatedString(thermalText))
                            Toast.makeText(context, "রসিদের টেক্সট কপি হয়েছে", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "কপি", modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}

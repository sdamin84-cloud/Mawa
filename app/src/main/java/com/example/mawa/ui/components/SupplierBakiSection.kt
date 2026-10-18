package com.example.mawa.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.MawaPrimary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Full Supplier Baki Section (মহাজন / ডিলার বাকি ও দেনা খাতা)
 */
@Composable
fun SupplierBakiSection(
    supplierInvoices: List<TransactionEntity>,
    supplierCompanies: List<SupplierCompanyEntity>,
    onAddSupplierDue: (companyName: String, srPhone: String, amount: Double, note: String, promisedDate: Long) -> Unit,
    onPaySupplierDue: (transactionId: Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var payingInvoice by remember { mutableStateOf<TransactionEntity?>(null) }

    val pendingList = remember(supplierInvoices) {
        supplierInvoices.filter { it.paymentStatus == "DUE_PAYMENT" }
    }

    val totalSupplierPayable = remember(pendingList) {
        pendingList.sumOf { it.amount }
    }

    val companyGroups = remember(pendingList) {
        pendingList.groupBy { it.productName?.ifBlank { it.note.ifBlank { "অন্যান্য সাপ্লায়ার" } } ?: "অন্যান্য সাপ্লায়ার" }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    val filteredList = remember(pendingList, searchQuery) {
        if (searchQuery.isBlank()) pendingList
        else pendingList.filter {
            (it.productName ?: "").contains(searchQuery, ignoreCase = true) ||
            (it.note).contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Payable Summary Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = FinancialWarning.copy(alpha = 0.08f)),
            border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "মোট মহাজন / সাপ্লায়ার দেনা",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = BengaliUtils.formatTaka(totalSupplierPayable),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = FinancialWarning
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = FinancialWarning.copy(alpha = 0.15f)
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "বকেয়া চালান",
                                style = MaterialTheme.typography.labelSmall,
                                color = FinancialWarning
                            )
                            Text(
                                text = "${BengaliUtils.toBanglaDigits(pendingList.size.toLong())} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FinancialWarning
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = FinancialWarning.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddDialog = true },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("মহাজন দেনা যোগ", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = { showReportDialog = true },
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, FinancialWarning)
                    ) {
                        Icon(Icons.Default.Print, null, modifier = Modifier.size(16.dp), tint = FinancialWarning)
                        Spacer(Modifier.width(4.dp))
                        Text("দেনা রিপোর্ট", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = FinancialWarning)
                    }
                }
            }
        }

        // 2. Graphical Breakdown of Supplier Payables
        if (companyGroups.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "কোম্পানিভিত্তিক মহাজন বাকি (Graphical Chart)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    val maxCompanyDue = companyGroups.values.maxOrNull()?.coerceAtLeast(1.0) ?: 1.0
                    companyGroups.entries.take(5).forEach { (company, amount) ->
                        val ratio = (amount / maxCompanyDue).toFloat().coerceIn(0f, 1f)
                        val animRatio by animateFloatAsState(targetValue = ratio, animationSpec = tween(500), label = "comp_bar")
                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = company,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = BengaliUtils.formatTaka(amount),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialWarning
                                )
                            }
                            Spacer(modifier = Modifier.height(3.dp))
                            LinearProgressIndicator(
                                progress = { animRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = FinancialWarning,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 3. Search Field
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("কোম্পানি বা মালের নাম খুঁজুন...") },
            leadingIcon = { Icon(Icons.Default.Search, null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        // 4. Pending Invoices List
        if (filteredList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Handshake, null, modifier = Modifier.size(40.dp), tint = FinancialPositive)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("কোনো মহাজন বা কোম্পানির দেনা নেই!", fontWeight = FontWeight.Bold)
                    Text("দোকানের ক্রয়কৃত সকল চালান পরিশোধ রয়েছে।", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                filteredList.forEach { invoice ->
                    SupplierInvoiceCard(
                        invoice = invoice,
                        companies = supplierCompanies,
                        onPayClick = { payingInvoice = invoice },
                        onShareInvoice = {
                            val compName = invoice.productName?.ifBlank { invoice.note } ?: "সাপ্লায়ার"
                            val text = "মহাজন বকেয়া চালান:\nকোম্পানি: $compName\nপরিমাণ: ${BengaliUtils.formatTaka(invoice.amount)}\nতারিখ: ${SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(invoice.timestamp))}"
                            val intent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, text)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(intent, "চালান শেয়ার করুন"))
                        }
                    )
                }
            }
        }
    }

    // Dialog: Add Supplier Due
    if (showAddDialog) {
        AddSupplierDueDialog(
            companies = supplierCompanies,
            onDismiss = { showAddDialog = false },
            onConfirm = { compName, srPhone, amount, note, date ->
                onAddSupplierDue(compName, srPhone, amount, note, date)
                showAddDialog = false
            }
        )
    }

    // Dialog: Confirm Payment
    if (payingInvoice != null) {
        val target = payingInvoice!!
        AlertDialog(
            onDismissRequest = { payingInvoice = null },
            icon = { Icon(Icons.Default.Payments, null, tint = FinancialPositive, modifier = Modifier.size(36.dp)) },
            title = { Text("মহাজন দেনা পরিশোধ") },
            text = {
                Column {
                    Text("আপনি কি নিশ্চিত যে নিচের বকেয়াটি ক্যাশ থেকে পরিশোধ করতে চান?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("কোম্পানি: ${target.productName?.ifBlank { target.note } ?: "সাপ্লায়ার"}", fontWeight = FontWeight.Bold)
                    Text("পরিমাণ: ${BengaliUtils.formatTaka(target.amount)}", fontWeight = FontWeight.Bold, color = FinancialPositive)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onPaySupplierDue(target.id)
                        payingInvoice = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive)
                ) {
                    Text("হ্যাঁ, পরিশোধ সম্পন্ন করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { payingInvoice = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Full Supplier Report
    if (showReportDialog) {
        SupplierReportDialog(
            invoices = pendingList,
            totalPayable = totalSupplierPayable,
            onDismiss = { showReportDialog = false }
        )
    }
}

@Composable
private fun SupplierInvoiceCard(
    invoice: TransactionEntity,
    companies: List<SupplierCompanyEntity>,
    onPayClick: () -> Unit,
    onShareInvoice: () -> Unit
) {
    val context = LocalContext.current
    val title = invoice.productName?.ifBlank { invoice.note.ifBlank { "কোম্পানি চালান" } } ?: "কোম্পানি চালান"
    val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(invoice.timestamp))

    // Find company SR phone if matched
    val matchedComp = companies.find { it.companyName.equals(title, ignoreCase = true) }
    val srPhone = matchedComp?.srPhone ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(dateStr, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = BengaliUtils.formatTaka(invoice.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = FinancialWarning
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = FinancialNegative.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "বকেয়া দেনা",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = FinancialNegative,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            if (invoice.note.isNotBlank() && invoice.note != title) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "নোট: ${invoice.note}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(modifier = Modifier.height(8.dp))

            // Actions: Call SR, Share, Pay
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (srPhone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$srPhone"))
                                context.startActivity(callIntent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "SR কল", tint = MawaPrimary, modifier = Modifier.size(18.dp))
                        }
                    }
                    IconButton(
                        onClick = onShareInvoice,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    }
                }

                Button(
                    onClick = onPayClick,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("নগদ পরিশোধ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AddSupplierDueDialog(
    companies: List<SupplierCompanyEntity>,
    onDismiss: () -> Unit,
    onConfirm: (companyName: String, srPhone: String, amount: Double, note: String, promisedDate: Long) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var srPhone by remember { mutableStateOf("") }
    var amountStr by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("নতুন মহাজন দেনা / চালান এন্ট্রি") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("কোম্পানি / মহাজনের নাম") },
                    placeholder = { Text("যেমন: প্রাণ, স্কয়ার, মেঘনা ইত্যাদি") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amountStr,
                    onValueChange = { amountStr = it },
                    label = { Text("চালানের বকেয়া টাকা (৳)") },
                    placeholder = { Text("যেমন: ৫০০০") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = srPhone,
                    onValueChange = { srPhone = it },
                    label = { Text("SR বা প্রতিনিধির মোবাইল নম্বর (ঐচ্ছিক)") },
                    placeholder = { Text("017XXXXXXXX") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("পণ্যের বিবরণ / চালান মেমো নং") },
                    placeholder = { Text("যেমন: তেল ও আটার চালান") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountStr.toDoubleOrNull() ?: 0.0
                    if (companyName.isNotBlank() && amount > 0) {
                        onConfirm(companyName.trim(), srPhone.trim(), amount, note.trim(), 0L)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

@Composable
private fun SupplierReportDialog(
    invoices: List<TransactionEntity>,
    totalPayable: Double,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val reportDate = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📑 মহাজন দেনা খতিয়ান রিপোর্ট", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, null)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(380.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = FinancialWarning.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("মোট মহাজন দেনা:")
                            Text(BengaliUtils.formatTaka(totalPayable), fontWeight = FontWeight.Bold, color = FinancialWarning)
                        }
                        Text("তারিখ: $reportDate", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(invoices) { inv ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = inv.productName?.ifBlank { inv.note } ?: "কোম্পানি চালান",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date(inv.timestamp)),
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = BengaliUtils.formatTaka(inv.amount),
                                    fontWeight = FontWeight.Bold,
                                    color = FinancialWarning,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val sb = StringBuilder()
                    sb.append("📑 মহাজন ও সাপ্লায়ার দেনা খতিয়ান\n")
                    sb.append("তারিখ: $reportDate\n\n")
                    sb.append("মোট দেনা: ${BengaliUtils.formatTaka(totalPayable)}\n\n")
                    sb.append("--- চালান তালিকা ---\n")
                    invoices.forEachIndexed { i, inv ->
                        val name = inv.productName?.ifBlank { inv.note } ?: "কোম্পানি চালান"
                        sb.append("${i + 1}. $name: ${BengaliUtils.formatTaka(inv.amount)}\n")
                    }
                    val intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, sb.toString())
                        type = "text/plain"
                    }
                    context.startActivity(Intent.createChooser(intent, "মহাজন রিপোর্ট শেয়ার"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("শেয়ার ও প্রিন্ট")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

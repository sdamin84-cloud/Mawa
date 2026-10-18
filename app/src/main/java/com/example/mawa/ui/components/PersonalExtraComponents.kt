package com.example.mawa.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.LoanType
import com.example.mawa.util.PersonalLoanItem
import com.example.mawa.util.PersonalLoanManager
import com.example.mawa.util.SavingsGoalItem
import com.example.mawa.util.SavingsGoalManager
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialWarning
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// =========================================================================
// 1. Personal Loans / Hawlat Section (ব্যক্তিগত ধার-দেনা ও হাওলাত)
// =========================================================================

@Composable
fun PersonalLoansSection(
    onRecordRepaymentAsTransaction: ((isIncome: Boolean, amount: Double, title: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember { PersonalLoanManager.getInstance(context) }
    val loans by manager.loans.collectAsStateWithLifecycle()

    var showAddLoanDialog by remember { mutableStateOf(false) }
    var loanToRepay by remember { mutableStateOf<PersonalLoanItem?>(null) }
    var loanToDelete by remember { mutableStateOf<PersonalLoanItem?>(null) }
    var selectedFilter by remember { mutableStateOf("সকল") }

    val receivableTotal = loans.filter { it.type == LoanType.GIVEN }.sumOf { it.remainingAmount }
    val payableTotal = loans.filter { it.type == LoanType.TAKEN }.sumOf { it.remainingAmount }

    val filteredLoans = remember(loans, selectedFilter) {
        when (selectedFilter) {
            "পাবো" -> loans.filter { it.type == LoanType.GIVEN && !it.isSettled }
            "দেবো" -> loans.filter { it.type == LoanType.TAKEN && !it.isSettled }
            "বাকি" -> loans.filter { !it.isSettled }
            "পরিশোধিত" -> loans.filter { it.isSettled }
            else -> loans
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Summary Cards: আমি পাবো vs আমি দেবো
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Handshake,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "ব্যক্তিগত ধার-দেনা ও ঋণ খাতা",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "বন্ধু-বান্ধব ও পরিচিতদের সাথে লেনদেন",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Button(
                        onClick = { showAddLoanDialog = true },
                        modifier = Modifier.height(34.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ধার যোগ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // আমি পাবো (Receivable)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = FinancialPositive.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, FinancialPositive.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("আমি পাবো (ধার দিয়েছি)", style = MaterialTheme.typography.labelSmall, color = FinancialPositive)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = BengaliUtils.formatTaka(receivableTotal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = FinancialPositive
                            )
                        }
                    }

                    // আমি দেবো (Payable)
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        color = FinancialNegative.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, FinancialNegative.copy(alpha = 0.25f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("আমি দেবো (ধার নিয়েছি)", style = MaterialTheme.typography.labelSmall, color = FinancialNegative)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = BengaliUtils.formatTaka(payableTotal),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = FinancialNegative
                            )
                        }
                    }
                }
            }
        }

        // Filter chips
        val filters = listOf("সকল", "পাবো", "দেবো", "বাকি", "পরিশোধিত")
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { f ->
                val isSel = selectedFilter == f
                FilterChip(
                    selected = isSel,
                    onClick = { selectedFilter = f },
                    label = { Text(f, fontSize = 12.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        // Loans List
        if (filteredLoans.isEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Handshake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "কোনো ধার-দেনার হিসাব পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredLoans.forEach { loan ->
                    val isReceivable = loan.type == LoanType.GIVEN
                    val color = if (isReceivable) FinancialPositive else FinancialNegative
                    val dateStr = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()).format(Date(loan.dateMillis))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(color.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = if (isReceivable) "পাবো" else "দেবো",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = color
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = loan.personName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (loan.phone.isNotBlank()) {
                                            Text(
                                                text = loan.phone,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = BengaliUtils.formatTaka(loan.remainingAmount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                    Text(
                                        text = if (loan.isSettled) "সম্পূর্ণ পরিশোধিত ✅" else "বকেয়া আছে",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (loan.isSettled) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (loan.note.isNotBlank() || loan.paidAmount > 0) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    if (loan.note.isNotBlank()) {
                                        Text(
                                            text = "নোট: ${loan.note}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    Text(
                                        text = "মোট: ৳${BengaliUtils.toBanglaDigits(loan.totalAmount.toLong())} (পরিশোধ: ৳${BengaliUtils.toBanglaDigits(loan.paidAmount.toLong())})",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            if (!loan.isSettled) {
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { loan.progressRatio },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(5.dp)
                                        .clip(RoundedCornerShape(3.dp)),
                                    color = color,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                            Spacer(modifier = Modifier.height(8.dp))

                            // Action buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = dateStr,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (loan.phone.isNotBlank()) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${loan.phone}"))
                                                try { context.startActivity(intent) } catch (_: Exception) {}
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Call, contentDescription = "কল", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }

                                        IconButton(
                                            onClick = {
                                                val msg = if (isReceivable) {
                                                    "আসসালামু আলাইকুম ${loan.personName} ভাই, ব্যক্তিগত ধার বাবদ ৳${loan.remainingAmount.toInt()} পাওনার বিষয়টি স্মরণ করিয়ে দেওয়া হলো। ধন্যবাদ।"
                                                } else {
                                                    "আসসালামু আলাইকুম ${loan.personName} ভাই, আপনার কাছ থেকে নেওয়া ধারের ৳${loan.remainingAmount.toInt()} দ্রুত পরিশোধের চেষ্টা করছি। ধন্যবাদ।"
                                                }
                                                val sendIntent = Intent().apply {
                                                    action = Intent.ACTION_SEND
                                                    putExtra(Intent.EXTRA_TEXT, msg)
                                                    type = "text/plain"
                                                }
                                                try { context.startActivity(Intent.createChooser(sendIntent, "মেসেজ পাঠান")) } catch (_: Exception) {}
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = "শেয়ার", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                        }
                                    }

                                    if (!loan.isSettled) {
                                        Button(
                                            onClick = { loanToRepay = loan },
                                            modifier = Modifier.height(30.dp),
                                            shape = RoundedCornerShape(8.dp),
                                            colors = ButtonDefaults.buttonColors(containerColor = color),
                                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                        ) {
                                            Text(if (isReceivable) "আদায়" else "পরিশোধ", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Spacer(modifier = Modifier.width(4.dp))
                                    }

                                    IconButton(
                                        onClick = { loanToDelete = loan },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "মুছুন", tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Loan
    if (showAddLoanDialog) {
        var personName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var loanType by remember { mutableStateOf(LoanType.GIVEN) }
        var amountStr by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddLoanDialog = false },
            title = { Text("নতুন ধার-দেনার এন্ট্রি", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // Type selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = loanType == LoanType.GIVEN,
                            onClick = { loanType = LoanType.GIVEN },
                            label = { Text("আমি পাবো (ধার দিয়েছি)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinancialPositive,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = loanType == LoanType.TAKEN,
                            onClick = { loanType = LoanType.TAKEN },
                            label = { Text("আমি দেবো (ধার নিয়েছি)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = FinancialNegative,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = personName,
                        onValueChange = { personName = it },
                        label = { Text("ব্যক্তির নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("মোবাইল নম্বর (ঐচ্ছিক)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("ধারের পরিমাণ (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("নোট বা কারণ (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (personName.isNotBlank() && amt > 0) {
                            manager.addLoan(
                                personName = personName,
                                phone = phone,
                                type = loanType,
                                amount = amt,
                                note = note
                            )
                            showAddLoanDialog = false
                        }
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddLoanDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Dialog: Record Repayment / Collection
    if (loanToRepay != null) {
        val loan = loanToRepay!!
        var paymentStr by remember { mutableStateOf(loan.remainingAmount.toInt().toString()) }
        var logAsPersonalTx by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { loanToRepay = null },
            title = {
                Text(
                    text = if (loan.type == LoanType.GIVEN) "${loan.personName}-এর থেকে টাকা আদায়"
                    else "${loan.personName}-কে টাকা পরিশোধ",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "মোট বকেয়া: ৳${BengaliUtils.toBanglaDigits(loan.remainingAmount.toLong())}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = paymentStr,
                        onValueChange = { paymentStr = it },
                        label = { Text("পরিশোধিত টাকা (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { logAsPersonalTx = !logAsPersonalTx }
                    ) {
                        Checkbox(
                            checked = logAsPersonalTx,
                            onCheckedChange = { logAsPersonalTx = it },
                            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (loan.type == LoanType.GIVEN) "ব্যক্তিগত আয়ে (ক্যাশে) যোগ করুন"
                            else "ব্যক্তিগত খরচে (ক্যাশ থেকে) বাদ দিন",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = paymentStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            manager.recordRepayment(loan.id, amt)
                            if (logAsPersonalTx && onRecordRepaymentAsTransaction != null) {
                                val isIncome = loan.type == LoanType.GIVEN
                                val titleStr = if (isIncome) "ধার ফেরত: ${loan.personName}" else "ধার পরিশোধ: ${loan.personName}"
                                onRecordRepaymentAsTransaction(isIncome, amt, titleStr)
                            }
                            loanToRepay = null
                        }
                    }
                ) {
                    Text("নিশ্চিত করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { loanToRepay = null }) { Text("বাতিল") }
            }
        )
    }

    // Dialog: Delete Loan
    if (loanToDelete != null) {
        AlertDialog(
            onDismissRequest = { loanToDelete = null },
            title = { Text("ধার মুছবেন?", fontWeight = FontWeight.Bold) },
            text = { Text("${loanToDelete!!.personName}-এর ধারের হিসাবটি মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        manager.deleteLoan(loanToDelete!!.id)
                        loanToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { loanToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

// =========================================================================
// 2. Savings Goals Section (ব্যক্তিগত সঞ্চয় লক্ষ্যমাত্রা)
// =========================================================================

@Composable
fun SavingsGoalsSection(
    onAddSavingsTransaction: ((amount: Double, title: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val manager = remember { SavingsGoalManager.getInstance(context) }
    val goals by manager.goals.collectAsStateWithLifecycle()

    var showAddGoalDialog by remember { mutableStateOf(false) }
    var goalToAddSavings by remember { mutableStateOf<SavingsGoalItem?>(null) }
    var goalToDelete by remember { mutableStateOf<SavingsGoalItem?>(null) }

    val totalTarget = goals.sumOf { it.targetAmount }
    val totalSaved = goals.sumOf { it.savedAmount }
    val overallRatio = if (totalTarget > 0) (totalSaved / totalTarget).toFloat().coerceIn(0f, 1f) else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(FinancialWarning.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = FinancialWarning,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "🎯 সঞ্চয় লক্ষ্যমাত্রা ও ডিপিএস",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "মোট জমানো: ${BengaliUtils.formatTaka(totalSaved)} / ${BengaliUtils.formatTaka(totalTarget)} (${BengaliUtils.toBanglaDigits((overallRatio * 100).toInt().toLong())}%)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = { showAddGoalDialog = true },
                    modifier = Modifier.height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("নতুন লক্ষ্য", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Goals list
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                goals.forEach { goal ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = goal.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (goal.note.isNotBlank()) {
                                        Text(
                                            text = goal.note,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedButton(
                                        onClick = { goalToAddSavings = goal },
                                        modifier = Modifier.height(30.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp)
                                    ) {
                                        Icon(Icons.Default.Add, null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text("জমান", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    IconButton(
                                        onClick = { goalToDelete = goal },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.outlineVariant, modifier = Modifier.size(15.dp))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            LinearProgressIndicator(
                                progress = { goal.progressRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = if (goal.isCompleted) FinancialPositive else FinancialWarning,
                                trackColor = MaterialTheme.colorScheme.surfaceVariant
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (goal.isCompleted) "🎉 লক্ষ্য পূরণ হয়েছে!"
                                    else "বাকি আছে: ${BengaliUtils.formatTaka(goal.remainingAmount)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (goal.isCompleted) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = if (goal.isCompleted) FontWeight.Bold else FontWeight.Normal
                                )

                                Text(
                                    text = "${BengaliUtils.formatTaka(goal.savedAmount)} / ${BengaliUtils.formatTaka(goal.targetAmount)} (${BengaliUtils.toBanglaDigits((goal.progressRatio * 100).toInt().toLong())}%)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (goal.isCompleted) FinancialPositive else FinancialWarning
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Add Savings to Goal
    if (goalToAddSavings != null) {
        val goal = goalToAddSavings!!
        var amountStr by remember { mutableStateOf("") }
        var logAsTx by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { goalToAddSavings = null },
            title = { Text("${goal.title}-এ টাকা জমান", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "কত টাকা যোগ করতে চান?",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = amountStr,
                        onValueChange = { amountStr = it },
                        label = { Text("জমানো টাকার পরিমাণ (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { logAsTx = !logAsTx }
                    ) {
                        Checkbox(
                            checked = logAsTx,
                            onCheckedChange = { logAsTx = it },
                            colors = CheckboxDefaults.colors(checkedColor = FinancialWarning)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "ব্যক্তিগত সঞ্চয় খতিয়ানে যোগ করুন",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = amountStr.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            manager.addSavings(goal.id, amt)
                            if (logAsTx && onAddSavingsTransaction != null) {
                                onAddSavingsTransaction(amt, "সঞ্চয়: ${goal.title}")
                            }
                            goalToAddSavings = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FinancialWarning)
                ) {
                    Text("সংরক্ষণ")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToAddSavings = null }) { Text("বাতিল") }
            }
        )
    }

    // Dialog: New Goal
    if (showAddGoalDialog) {
        var title by remember { mutableStateOf("") }
        var targetStr by remember { mutableStateOf("") }
        var initialSavedStr by remember { mutableStateOf("") }
        var note by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddGoalDialog = false },
            title = { Text("নতুন সঞ্চয় লক্ষ্য নির্ধারণ", fontWeight = FontWeight.Bold) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("লক্ষ্যের নাম (যেমন: নতুন বাইক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = targetStr,
                        onValueChange = { targetStr = it },
                        label = { Text("মোট লক্ষ্যমাত্রা টাকা (৳)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = initialSavedStr,
                        onValueChange = { initialSavedStr = it },
                        label = { Text("ইতিমধ্যে জমানো আছে (ঐচ্ছিক)") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = note,
                        onValueChange = { note = it },
                        label = { Text("নোট বা বিবরণ (ঐচ্ছিক)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val target = targetStr.toDoubleOrNull() ?: 0.0
                        val initial = initialSavedStr.toDoubleOrNull() ?: 0.0
                        if (title.isNotBlank() && target > 0) {
                            manager.addGoal(title, target, initial, note)
                            showAddGoalDialog = false
                        }
                    }
                ) {
                    Text("যোগ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddGoalDialog = false }) { Text("বাতিল") }
            }
        )
    }

    // Dialog: Delete Goal
    if (goalToDelete != null) {
        AlertDialog(
            onDismissRequest = { goalToDelete = null },
            title = { Text("লক্ষ্যটি মুছবেন?", fontWeight = FontWeight.Bold) },
            text = { Text("${goalToDelete!!.title} লক্ষ্যটি তালিকা থেকে মুছে ফেলতে চান?") },
            confirmButton = {
                Button(
                    onClick = {
                        manager.deleteGoal(goalToDelete!!.id)
                        goalToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { goalToDelete = null }) { Text("বাতিল") }
            }
        )
    }
}

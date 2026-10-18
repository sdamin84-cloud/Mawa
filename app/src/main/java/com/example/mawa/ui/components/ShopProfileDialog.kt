package com.example.mawa.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

/**
 * ShopProfileDialog:
 * Allows shopkeepers to create or update their own Shop Profile in MAWA.
 * The shop name is prominently displayed across invoices, customer memos, and home header,
 * while the app branding "MAWA" remains preserved and co-displayed as requested.
 */
@Composable
fun ShopProfileDialog(
    currentSettings: ShopSettingsEntity?,
    onDismiss: () -> Unit,
    onSave: (ShopSettingsEntity) -> Unit
) {
    var shopName by remember {
        mutableStateOf(
            if (currentSettings?.shopName != null && currentSettings.shopName != "মাওয়া ডিজিটাল খাতা") {
                currentSettings.shopName
            } else ""
        )
    }
    var ownerName by remember {
        mutableStateOf(
            if (currentSettings?.ownerName != null && currentSettings.ownerName != "দোকানদার") {
                currentSettings.ownerName
            } else ""
        )
    }
    var phone by remember { mutableStateOf(currentSettings?.phone ?: "") }
    var bkashNumber by remember { mutableStateOf(currentSettings?.bkashNumber ?: "") }
    var nagadNumber by remember { mutableStateOf(currentSettings?.nagadNumber ?: "") }
    var bankDetails by remember { mutableStateOf(currentSettings?.bankDetails ?: "") }
    var openingCash by remember {
        val initialCash = currentSettings?.openingBalance ?: 0.0
        mutableStateOf(if (initialCash > 0) (if (initialCash % 1.0 == 0.0) initialCash.toLong().toString() else initialCash.toString()) else "")
    }

    var showError by remember { mutableStateOf(false) }

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
                                listOf(MawaPrimary, Color(0xFF0D9488))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storefront,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "দোকান প্রোফাইল তৈরি ও সেটিংস",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MAWA এপে আপনার দোকানের নাম ও পরিচিতি",
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
                    .verticalScroll(rememberScrollState())
            ) {
                // Live Co-Branding Preview Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "লাইভ প্রিভিউ (মেমো ও খাতায় যেমন দেখাবে)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 11.sp
                            )
                            // App Brand Tag
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Text(
                                        text = "MAWA অ্যাপ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Black,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storefront,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (shopName.isNotBlank()) shopName else "আপনার দোকানের নাম",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (shopName.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                val subDetails = buildList {
                                    if (ownerName.isNotBlank()) add("স্বত্বাধিকারী: $ownerName")
                                    if (phone.isNotBlank()) add("মোবাইল: $phone")
                                }.joinToString(" · ")

                                Text(
                                    text = subDetails.ifBlank { "স্বত্বাধিকারী ও মোবাইল নম্বর লিখুন" },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                // Clarification explanation banner
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "এপের নাম 'MAWA' থাকবে এবং আপনার দোকানের নাম প্রোফাইল ও সব লেনদেন রসিদে প্রধান নাম হিসেবে শো করবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Input: Shop Name (দোকানের নাম)
                OutlinedTextField(
                    value = shopName,
                    onValueChange = {
                        shopName = it
                        if (it.isNotBlank()) showError = false
                    },
                    label = { Text("দোকানের নাম *") },
                    placeholder = { Text("যেমন: জনতা ট্রেডার্স / সততা স্টোর") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    isError = showError,
                    supportingText = {
                        if (showError) {
                            Text("দোকানের নাম লিখুন", color = MaterialTheme.colorScheme.error)
                        } else {
                            Text("হেডার, মেমো, কাস্টমার বাকি মেসেজে এই নাম শো করবে")
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shop_profile_name"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input: Owner Name (মালিক / প্রোপাইটর)
                OutlinedTextField(
                    value = ownerName,
                    onValueChange = { ownerName = it },
                    label = { Text("স্বত্বাধিকারী / মালিকের নাম") },
                    placeholder = { Text("যেমন: মো: আল-আমিন") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shop_profile_owner"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input: Phone (মোবাইল নম্বর)
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("দোকানের মোবাইল নম্বর") },
                    placeholder = { Text("যেমন: ০১৭১২-৩৪৫৬৭৮") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    supportingText = {
                        Text("কাস্টমারদের তাগাদা মেসেজ ও হোয়াটসঅ্যাপে পাঠাতে ব্যবহৃত হবে")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shop_profile_phone"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Payment Gateways & QR Section Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCode,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "পেমেন্ট নম্বর ও QR কোড (মেমো ও তাগাদার জন্য)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input: bKash Number (বিকাশ নম্বর)
                OutlinedTextField(
                    value = bkashNumber,
                    onValueChange = { bkashNumber = it },
                    label = { Text("বিকাশ নম্বর (Personal / Merchant)") },
                    placeholder = { Text("যেমন: ০১৭১২-৩৪৫৬৭৮") },
                    leadingIcon = {
                        Text(
                            text = "bKash",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFE2136E),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shop_profile_bkash"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input: Nagad Number (নগদ নম্বর)
                OutlinedTextField(
                    value = nagadNumber,
                    onValueChange = { nagadNumber = it },
                    label = { Text("নগদ নম্বর (Personal / Merchant)") },
                    placeholder = { Text("যেমন: ০১৮১২-৩৪৫৬৭৮") },
                    leadingIcon = {
                        Text(
                            text = "Nagad",
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFF7941D),
                            fontSize = 11.sp,
                            modifier = Modifier.padding(start = 10.dp)
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shop_profile_nagad"),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Input: Bank Details (ব্যাংক একাউন্ট বিবরণ)
                OutlinedTextField(
                    value = bankDetails,
                    onValueChange = { bankDetails = it },
                    label = { Text("ব্যাংক একাউন্ট বিবরণ (ঐচ্ছিক)") },
                    placeholder = { Text("যেমন: ইসলামী ব্যাংক, গুলশান শাখা, A/C: ২০৫০...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AccountBalance,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shop_profile_bank"),
                    shape = RoundedCornerShape(10.dp)
                )

                // Live Generated QR Preview
                val activePaymentNumber = bkashNumber.ifBlank { nagadNumber.ifBlank { phone } }
                if (activePaymentNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            val qrPayload = com.example.mawa.util.QrCodeGenerator.buildPaymentQrPayload(
                                shopName = shopName.ifBlank { "MAWA দোকান" },
                                phone = phone,
                                bkashNumber = bkashNumber,
                                nagadNumber = nagadNumber,
                                bankDetails = bankDetails
                            )
                            com.example.mawa.util.QrCodeImage(
                                qrContent = qrPayload,
                                size = 80.dp
                            )
                            Column {
                                Text(
                                    text = "স্বয়ংক্রিয় পেমেন্ট QR তৈরি হয়েছে",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "ডিজিটাল মেমো ও বাকি তাগাদায় কাস্টমার এই QR স্ক্যান করে সরাসরি টাকা পাঠাতে পারবে।",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                // Input: Opening Balance (সাবেক ক্যাশ)
                OutlinedTextField(
                    value = openingCash,
                    onValueChange = {
                        if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                            openingCash = it
                        }
                    },
                    label = { Text("সাবেক ক্যাশ / প্রারম্ভিক ক্যাশ (৳)") },
                    placeholder = { Text("0") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Savings,
                            contentDescription = null,
                            tint = FinancialPositive
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    supportingText = {
                        Text("দোকান শুরুর সময় ক্যাশ বাক্সে থাকা নগদ টাকা")
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_shop_profile_opening_cash"),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalShopName = shopName.trim().ifBlank { "আমার দোকান" }
                    val cash = openingCash.toDoubleOrNull() ?: 0.0
                    val qrPayload = com.example.mawa.util.QrCodeGenerator.buildPaymentQrPayload(
                        shopName = finalShopName,
                        phone = phone.trim(),
                        bkashNumber = bkashNumber.trim(),
                        nagadNumber = nagadNumber.trim(),
                        bankDetails = bankDetails.trim()
                    )
                    val updated = ShopSettingsEntity(
                        id = currentSettings?.id ?: 1,
                        shopName = finalShopName,
                        ownerName = ownerName.trim().ifBlank { "দোকানদার" },
                        phone = phone.trim(),
                        bkashNumber = bkashNumber.trim(),
                        nagadNumber = nagadNumber.trim(),
                        bankDetails = bankDetails.trim(),
                        qrCodeData = qrPayload,
                        openingBalance = cash,
                        currencySymbol = currentSettings?.currencySymbol ?: "৳",
                        appMode = currentSettings?.appMode ?: "BOTH",
                        isModeConfigured = true
                    )
                    onSave(updated)
                },
                modifier = Modifier.testTag("btn_save_shop_profile"),
                colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("প্রোফাইল সংরক্ষণ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("btn_cancel_shop_profile")
            ) {
                Text("বাতিল")
            }
        }
    )
}

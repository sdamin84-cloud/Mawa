package com.example.mawa.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer

/**
 * 3. সপ্তাহের ৭ দিনের কোম্পানি শিডিউল ক্যালেন্ডার (Weekly 7-Day Company Schedule Calendar)
 * 
 * দোকানের জন্য অত্যন্ত গুরুত্বপূর্ণ ফিচার:
 * - সপ্তাহের ৭ দিন (শনিবার থেকে শুক্রবার) কোন কোন কোম্পানির SR আসেন (অর্ডার দিন)
 * - কোন দিন ডেলিভারি মাল নামবে (ডেলিভারির দিন)
 * - আজকের দিন প্রমিনেন্টলি হাইলাইট করা
 * - SR-এর সাথে ১-ট্যাপে সরাসরি কল ও মেসেজ
 * - আজকের ডেলিভারির জন্য কত টাকা ক্যাশ রেডি রাখা লাগবে তার হিসাব
 */
@Composable
fun WeeklyCompanyScheduleCalendarDialog(
    supplierCompanies: List<SupplierCompanyEntity>,
    fordiItems: List<FordiItemEntity>,
    onDismiss: () -> Unit,
    onSaveCompany: (SupplierCompanyEntity) -> Unit,
    onOpenFordi: () -> Unit = {},
    onConfirmDeliveryReceived: (FordiItemEntity, isCashPaid: Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val todayBengaliDay = remember { BengaliUtils.getBengaliDayOfWeek(System.currentTimeMillis()) }
    val daysOfWeek = remember { BengaliUtils.banglaWeekdaysOrdered }

    var selectedDay by remember {
        mutableStateOf(if (todayBengaliDay in daysOfWeek) todayBengaliDay else daysOfWeek.first())
    }

    // Toggle between "দিনের বিস্তারিত" (Single Day) and "৭ দিনের পূর্ণাঙ্গ তালিকা" (All 7 Days Matrix)
    var isFullWeekMatrixView by remember { mutableStateOf(false) }

    // Dialog to add/edit company schedule
    var showEditCompanyDialog by remember { mutableStateOf(false) }
    var companyToEdit by remember { mutableStateOf<SupplierCompanyEntity?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 16.dp)
                .testTag("weekly_company_schedule_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MawaPrimaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = MawaPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "সপ্তাহের কোম্পানি শিডিউল ক্যালেন্ডার",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "আজ: ",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = FinancialPositiveContainer
                                ) {
                                    Text(
                                        text = todayBengaliDay,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialPositive,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Toggle Matrix / Single Day View
                        IconButton(
                            onClick = { isFullWeekMatrixView = !isFullWeekMatrixView },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = if (isFullWeekMatrixView) Icons.Default.DateRange else Icons.Default.ViewAgenda,
                                contentDescription = "ভিউ পরিবর্তন",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onDismiss, modifier = Modifier.size(36.dp)) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "বন্ধ করুন",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 7-Day Horizontal Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    daysOfWeek.forEach { dayName ->
                        val isDaySelected = dayName == selectedDay && !isFullWeekMatrixView
                        val isToday = dayName == todayBengaliDay
                        
                        // Count companies on this day
                        val orderCount = supplierCompanies.count { it.orderDay.contains(dayName, ignoreCase = true) }
                        val deliveryCount = supplierCompanies.count { it.deliveryDay.contains(dayName, ignoreCase = true) }
                        val totalActivities = orderCount + deliveryCount

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .clickable {
                                    selectedDay = dayName
                                    isFullWeekMatrixView = false
                                },
                            color = when {
                                isDaySelected -> MaterialTheme.colorScheme.primary
                                isToday -> MaterialTheme.colorScheme.primaryContainer
                                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            },
                            border = BorderStroke(
                                width = if (isToday) 1.5.dp else 1.dp,
                                color = when {
                                    isDaySelected -> MaterialTheme.colorScheme.primary
                                    isToday -> MaterialTheme.colorScheme.primary
                                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                }
                            ),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = dayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isDaySelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            isDaySelected -> MaterialTheme.colorScheme.onPrimary
                                            isToday -> MaterialTheme.colorScheme.primary
                                            else -> MaterialTheme.colorScheme.onSurface
                                        }
                                    )
                                    if (isToday) {
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .clip(CircleShape)
                                                .background(if (isDaySelected) MaterialTheme.colorScheme.onPrimary else FinancialPositive)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (totalActivities > 0) "${BengaliUtils.toBanglaDigits(totalActivities)} কোম্পানি" else "ফাঁকা",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 10.sp,
                                    color = when {
                                        isDaySelected -> MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                                        totalActivities > 0 -> MaterialTheme.colorScheme.onSurfaceVariant
                                        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action Bar: Add Company to this Day + Fast FMCG Defaults if empty
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isFullWeekMatrixView) "পূর্ণাঙ্গ সপ্তাহের শিডিউল চার্ট" else "$selectedDay এর শিডিউল",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Button(
                        onClick = {
                            companyToEdit = SupplierCompanyEntity(
                                companyName = "",
                                orderDay = selectedDay,
                                deliveryDay = getNextDayName(selectedDay)
                            )
                            showEditCompanyDialog = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+ কোম্পানি যোগ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Content View
                if (supplierCompanies.isEmpty()) {
                    // Empty State with 1-tap popular company setup
                    EmptyScheduleState(
                        onAddDefaultCompanies = {
                            val defaultList = getPopularBangladeshiCompanies()
                            defaultList.forEach { onSaveCompany(it) }
                            Toast.makeText(context, "জনপ্রিয় কোম্পানিগুলোর শিডিউল যুক্ত হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        onAddNew = {
                            companyToEdit = SupplierCompanyEntity(companyName = "", orderDay = selectedDay)
                            showEditCompanyDialog = true
                        }
                    )
                } else if (isFullWeekMatrixView) {
                    // 7-Day Matrix View
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(daysOfWeek) { day ->
                            DaySummaryMatrixCard(
                                dayName = day,
                                isToday = day == todayBengaliDay,
                                supplierCompanies = supplierCompanies,
                                onSelectDay = {
                                    selectedDay = day
                                    isFullWeekMatrixView = false
                                },
                                onEditCompany = { comp ->
                                    companyToEdit = comp
                                    showEditCompanyDialog = true
                                }
                            )
                        }
                    }
                } else {
                    // Single Day Detailed View
                    val orderCompanies = remember(supplierCompanies, selectedDay) {
                        supplierCompanies.filter { it.orderDay.contains(selectedDay, ignoreCase = true) }
                    }
                    val deliveryCompanies = remember(supplierCompanies, selectedDay) {
                        supplierCompanies.filter { it.deliveryDay.contains(selectedDay, ignoreCase = true) }
                    }

                    // Check Fordi scheduled items for this day's deliveries
                    val scheduledDeliveryFordiItems = remember(fordiItems, selectedDay) {
                        fordiItems.filter { !it.isPurchased && it.deliveryDay.contains(selectedDay, ignoreCase = true) }
                    }
                    val estimatedCashNeeded = remember(scheduledDeliveryFordiItems) {
                        scheduledDeliveryFordiItems.sumOf { (if (it.plannedQuantity > 0) it.plannedQuantity else 1.0) * it.purchaseRate }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Estimated Cash Banner for Deliveries
                        if (estimatedCashNeeded > 0) {
                            item {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp),
                                    color = FinancialWarningContainer.copy(alpha = 0.8f),
                                    border = BorderStroke(1.dp, FinancialWarning.copy(alpha = 0.5f))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Payments,
                                            contentDescription = null,
                                            tint = FinancialWarning,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "$selectedDay ডেলিভারির জন্য আনুমানিক ক্যাশ প্রস্তুত রাখুন:",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = "৳${BengaliUtils.toBanglaDigits(estimatedCashNeeded.toInt())} (${BengaliUtils.toBanglaDigits(scheduledDeliveryFordiItems.size)}টি পণ্যের ডেলিভারি)",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = FinancialWarning
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Section 1: Order Day Companies (অর্ডার নেওয়ার দিন)
                        item {
                            SectionHeaderBadge(
                                title = "📋 অর্ডার নেওয়ার দিন (এসআর আসবে)",
                                subtitle = if (orderCompanies.isNotEmpty()) "${BengaliUtils.toBanglaDigits(orderCompanies.size)}টি কোম্পানি" else "কোনো কোম্পানির অর্ডার নেই",
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (orderCompanies.isEmpty()) {
                            item {
                                EmptyDaySectionNote(
                                    text = "$selectedDay কোনো কোম্পানির অর্ডার দেওয়ার শিডিউল নেই।",
                                    onAddCompany = {
                                        companyToEdit = SupplierCompanyEntity(companyName = "", orderDay = selectedDay)
                                        showEditCompanyDialog = true
                                    }
                                )
                            }
                        } else {
                            items(orderCompanies, key = { "order_${it.id}" }) { comp ->
                                CompanyScheduleItemCard(
                                    company = comp,
                                    roleType = ScheduleRole.ORDER,
                                    fordiItems = fordiItems,
                                    onEdit = {
                                        companyToEdit = comp
                                        showEditCompanyDialog = true
                                    },
                                    onOpenFordi = onOpenFordi
                                )
                            }
                        }

                        // Section 2: Delivery Day Companies (ডেলিভারি আসার দিন)
                        item {
                            Spacer(modifier = Modifier.height(6.dp))
                            SectionHeaderBadge(
                                title = "🚚 মাল ডেলিভারির দিন (দোকানে আসবে)",
                                subtitle = if (deliveryCompanies.isNotEmpty()) "${BengaliUtils.toBanglaDigits(deliveryCompanies.size)}টি কোম্পানি" else "কোনো ডেলিভারি নেই",
                                color = FinancialPositive
                            )
                        }

                        if (deliveryCompanies.isEmpty()) {
                            item {
                                EmptyDaySectionNote(
                                    text = "$selectedDay কোনো কোম্পানির মাল আসার শিডিউল নেই।",
                                    onAddCompany = {
                                        companyToEdit = SupplierCompanyEntity(companyName = "", deliveryDay = selectedDay)
                                        showEditCompanyDialog = true
                                    }
                                )
                            }
                        } else {
                            items(deliveryCompanies, key = { "delivery_${it.id}" }) { comp ->
                                val compFordiItems = fordiItems.filter {
                                    !it.isPurchased && (it.companyName.equals(comp.companyName, ignoreCase = true) || it.productName.contains(comp.companyName, ignoreCase = true))
                                }
                                CompanyScheduleItemCard(
                                    company = comp,
                                    roleType = ScheduleRole.DELIVERY,
                                    fordiItems = compFordiItems,
                                    onEdit = {
                                        companyToEdit = comp
                                        showEditCompanyDialog = true
                                    },
                                    onOpenFordi = onOpenFordi,
                                    onConfirmDelivery = { item, isPaid ->
                                        onConfirmDeliveryReceived(item, isPaid)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Add / Edit Company Schedule Dialog
    if (showEditCompanyDialog) {
        EditCompanySrDialog(
            initialCompany = companyToEdit,
            onDismiss = {
                showEditCompanyDialog = false
                companyToEdit = null
            },
            onSave = { updated ->
                onSaveCompany(updated)
                showEditCompanyDialog = false
                companyToEdit = null
            }
        )
    }
}

enum class ScheduleRole {
    ORDER,
    DELIVERY
}

@Composable
private fun SectionHeaderBadge(
    title: String,
    subtitle: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CompanyScheduleItemCard(
    company: SupplierCompanyEntity,
    roleType: ScheduleRole,
    fordiItems: List<FordiItemEntity>,
    onEdit: () -> Unit,
    onOpenFordi: () -> Unit,
    onConfirmDelivery: (FordiItemEntity, Boolean) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    val isOrder = roleType == ScheduleRole.ORDER

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isOrder) MaterialTheme.colorScheme.primaryContainer else FinancialPositiveContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isOrder) Icons.Default.Business else Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = if (isOrder) MaterialTheme.colorScheme.primary else FinancialPositive,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = company.companyName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (company.srName.isNotBlank()) {
                            Text(
                                text = "SR: ${company.srName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Direct Call Button
                    if (company.srPhone.isNotBlank()) {
                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${company.srPhone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(FinancialPositiveContainer)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "কল করুন",
                                tint = FinancialPositive,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))

                        // SMS Button
                        IconButton(
                            onClick = {
                                val smsIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${company.srPhone}"))
                                context.startActivity(smsIntent)
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Message,
                                contentDescription = "মেসেজ পাঠান",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    // Edit Button
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "এডিট",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Schedule info pills (অর্ডারের দিন ও ডেলিভারির দিন)
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (company.orderDay.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "📋 অর্ডার: ${company.orderDay}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
                if (company.deliveryDay.isNotBlank()) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = FinancialPositiveContainer.copy(alpha = 0.6f)
                    ) {
                        Text(
                            text = "🚚 ডেলিভারি: ${company.deliveryDay}",
                            style = MaterialTheme.typography.labelSmall,
                            color = FinancialPositive,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // If this is Delivery Day, show items to receive
            if (!isOrder && fordiItems.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "প্রত্যাশিত ডেলিভারির পণ্য:",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    fordiItems.forEach { item ->
                        val qty = if (item.actualQuantity > 0) item.actualQuantity else item.plannedQuantity
                        val rate = if (item.actualRate > 0) item.actualRate else item.purchaseRate
                        val total = qty * rate

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• ${item.productName} (${BengaliUtils.formatQuantity(qty, item.unit)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "৳${BengaliUtils.toBanglaDigits(total.toInt())}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Button(
                                    onClick = { onConfirmDelivery(item, true) },
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialPositive),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp, vertical = 2.dp),
                                    modifier = Modifier.height(24.dp)
                                ) {
                                    Text("পেয়েছি", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else if (isOrder) {
                // Button to jump to Fordi to add items for this company
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "ফর্দে মাল লিখতে যান →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onOpenFordi() }
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun DaySummaryMatrixCard(
    dayName: String,
    isToday: Boolean,
    supplierCompanies: List<SupplierCompanyEntity>,
    onSelectDay: () -> Unit,
    onEditCompany: (SupplierCompanyEntity) -> Unit
) {
    val orderCompanies = supplierCompanies.filter { it.orderDay.contains(dayName, ignoreCase = true) }
    val deliveryCompanies = supplierCompanies.filter { it.deliveryDay.contains(dayName, ignoreCase = true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectDay() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isToday) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = BorderStroke(
            1.dp,
            if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    if (isToday) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = FinancialPositive
                        ) {
                            Text(
                                text = "আজ",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                            )
                        }
                    }
                }

                Text(
                    text = "বিস্তারিত দেখুন →",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (orderCompanies.isEmpty() && deliveryCompanies.isEmpty()) {
                Text(
                    text = "কোনো শিডিউল নির্ধারিত নেই",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            } else {
                if (orderCompanies.isNotEmpty()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📋 অর্ডার: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = orderCompanies.joinToString(", ") { it.companyName },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (deliveryCompanies.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🚚 ডেলিভারি: ",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = FinancialPositive
                        )
                        Text(
                            text = deliveryCompanies.joinToString(", ") { it.companyName },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyScheduleState(
    onAddDefaultCompanies: () -> Unit,
    onAddNew: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(MawaPrimaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MawaPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "কোনো কোম্পানির শিডিউল এখনো যোগ করা হয়নি",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "কোন দিন কোন কোম্পানির সেলসম্যান আসেন এবং কোন দিন ডেলিভারি নামবে তা সাজিয়ে রাখুন।",
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddDefaultCompanies,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Business, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("জনপ্রিয় কোম্পানিগুলোর শিডিউল লোড করুন")
            }
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(
                onClick = onAddNew,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("নিজে নতুন কোম্পানি যুক্ত করুন")
            }
        }
    }
}

@Composable
private fun EmptyDaySectionNote(
    text: String,
    onAddCompany: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "+ যোগ করুন",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onAddCompany() }
                    .padding(4.dp)
            )
        }
    }
}

private fun getNextDayName(currentDay: String): String {
    val days = listOf("শনিবার", "রবিবার", "সোমবার", "মঙ্গলবার", "বুধবার", "বৃহস্পতিবার", "শুক্রবার")
    val idx = days.indexOf(currentDay)
    return if (idx >= 0 && idx < days.size - 1) days[idx + 1] else days.first()
}

/**
 * Standard FMCG companies in Bangladesh for 1-tap quick setup
 */
private fun getPopularBangladeshiCompanies(): List<SupplierCompanyEntity> {
    return listOf(
        SupplierCompanyEntity(
            companyName = "প্রাণ (PRAN)",
            srName = "রফিক ভাই (প্রাণ SR)",
            srPhone = "01700000001",
            orderDay = "রবিবার",
            deliveryDay = "সোমবার",
            note = "জুস, চিপস, বিস্কুট"
        ),
        SupplierCompanyEntity(
            companyName = "স্কয়ার (Square)",
            srName = "জাকির হোসেন",
            srPhone = "01700000002",
            orderDay = "রবিবার",
            deliveryDay = "মঙ্গলবার",
            note = "রাধুনী মসলা, রুচি, সাবান"
        ),
        SupplierCompanyEntity(
            companyName = "ইউনিলিভার (Unilever)",
            srName = "মাহবুব আলম",
            srPhone = "01700000003",
            orderDay = "সোমবার",
            deliveryDay = "বুধবার",
            note = "লাক্স, হুইল, সার্ফ এক্সেল, সানসিল্ক"
        ),
        SupplierCompanyEntity(
            companyName = "অলিম্পিক (Olympic)",
            srName = "হাসান আলী",
            srPhone = "01700000004",
            orderDay = "শনিবার",
            deliveryDay = "সোমবার",
            note = "এনার্জি বিস্কুট, কুকিজ"
        ),
        SupplierCompanyEntity(
            companyName = "আকিজ (Akij / Mojo)",
            srName = "তানভীর",
            srPhone = "01700000005",
            orderDay = "মঙ্গলবার",
            deliveryDay = "বৃহস্পতিবার",
            note = "মোজো, ফ্রুটো, আকিজ আটা"
        ),
        SupplierCompanyEntity(
            companyName = "ফ্রেশ (মেঘনা গ্রুপ)",
            srName = "সাইফুল ইসলাম",
            srPhone = "01700000006",
            orderDay = "বুধবার",
            deliveryDay = "শনিবার",
            note = "ফ্রেশ সয়াবিন তেল, চিনি, ডাল"
        ),
        SupplierCompanyEntity(
            companyName = "তীর (সিটি গ্রুপ)",
            srName = "শামীম",
            srPhone = "01700000007",
            orderDay = "বৃহস্পতিবার",
            deliveryDay = "রবিবার",
            note = "তীর তেল, তীর আটা-ময়দা"
        )
    )
}

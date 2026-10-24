package com.example.mawa.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.mawa.data.local.entity.CompanyOrderEntity
import com.example.mawa.ui.components.MawaEmptyState
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class OrderFilterTab(val title: String) {
    PENDING("পেন্ডিং"),
    CONFIRMED("কনফার্মড"),
    CANCELLED("বাতিল")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderKhataScreen(
    viewModel: MawaViewModel,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val allOrders by viewModel.allCompanyOrders.collectAsStateWithLifecycle()
    val supplierCompanies by viewModel.allSupplierCompanies.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(OrderFilterTab.PENDING) }
    var showAddOrderDialog by remember { mutableStateOf(false) }
    var orderToConfirm by remember { mutableStateOf<CompanyOrderEntity?>(null) }
    var orderToDelete by remember { mutableStateOf<CompanyOrderEntity?>(null) }

    val filteredOrders = remember(allOrders, selectedTab, searchQuery) {
        val tabStatus = when (selectedTab) {
            OrderFilterTab.PENDING -> "PENDING"
            OrderFilterTab.CONFIRMED -> "CONFIRMED"
            OrderFilterTab.CANCELLED -> "CANCELLED"
        }
        allOrders.filter { order ->
            order.status == tabStatus &&
                    (searchQuery.isBlank() ||
                            order.companyName.contains(searchQuery, ignoreCase = true) ||
                            order.note.contains(searchQuery, ignoreCase = true))
        }
    }

    val pendingCount = remember(allOrders) { allOrders.count { it.status == "PENDING" } }
    val confirmedCount = remember(allOrders) { allOrders.count { it.status == "CONFIRMED" } }
    val cancelledCount = remember(allOrders) { allOrders.count { it.status == "CANCELLED" } }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 2.dp,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Top App Identity Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (onBack != null) {
                                IconButton(
                                    onClick = onBack,
                                    modifier = Modifier.testTag("btn_order_khata_back")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "ফিরে যান",
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }

                            // Store Icon Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE6F4EA),
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Storefront,
                                        contentDescription = "দোকান",
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "মাওয়া / MAWA",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color(0xFF0F766E),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                                Text(
                                    text = "অর্ডার খাতা",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 20.sp
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onOpenDrawer?.invoke() },
                                modifier = Modifier.testTag("btn_order_khata_menu")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "মেনু",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Rounded Search Bar: "সার্চ অর্ডার"
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = "সার্চ অর্ডার",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "সার্চ",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotBlank()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "মুছুন",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("input_search_order")
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // 3 Filter Tabs: পেন্ডিং, কনফার্মড, বাতিল
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                            .padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OrderFilterTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            val count = when (tab) {
                                OrderFilterTab.PENDING -> pendingCount
                                OrderFilterTab.CONFIRMED -> confirmedCount
                                OrderFilterTab.CANCELLED -> cancelledCount
                            }

                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { selectedTab = tab }
                                    .testTag("tab_order_${tab.name.lowercase()}"),
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(0xFFCCE8D7) else Color.Transparent
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${tab.title} (${BengaliUtils.toBanglaDigits(count.toLong())})",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color(0xFF095E32) else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.5.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddOrderDialog = true },
                containerColor = Color(0xFF0F766E),
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .shadow(elevation = 6.dp, shape = CircleShape)
                    .testTag("fab_add_company_order")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "নতুন অর্ডার",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { paddingValues ->
        if (filteredOrders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                MawaEmptyState(
                    icon = Icons.Default.Assignment,
                    title = when (selectedTab) {
                        OrderFilterTab.PENDING -> "কোনো পেন্ডিং অর্ডার নেই"
                        OrderFilterTab.CONFIRMED -> "এখনও কোনো কনফার্মড অর্ডার নেই"
                        OrderFilterTab.CANCELLED -> "কোনো বাতিল অর্ডার নেই"
                    },
                    subtitle = when (selectedTab) {
                        OrderFilterTab.PENDING -> "আজ কোম্পানি থেকে কী মাল অর্ডার করবেন তা মনে রাখার জন্য নিচের '+' চাপুন"
                        else -> "পেন্ডিং অর্ডার কনফার্ম করলে এখানে দেখতে পাবেন"
                    },
                    actionLabel = if (selectedTab == OrderFilterTab.PENDING) "নতুন অর্ডার লিখুন" else null,
                    onActionClick = { showAddOrderDialog = true }
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredOrders, key = { it.id }) { order ->
                    CompanyOrderCardItem(
                        order = order,
                        onConfirmClick = { orderToConfirm = order },
                        onCancelClick = {
                            viewModel.cancelCompanyOrder(order.id)
                            Toast.makeText(context, "অর্ডার বাতিল করা হয়েছে", Toast.LENGTH_SHORT).show()
                        },
                        onDeleteClick = { orderToDelete = order }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    // Dialog: Confirm Order Received
    orderToConfirm?.let { order ->
        var recordAsPurchase by remember { mutableStateOf(true) }
        AlertDialog(
            onDismissRequest = { orderToConfirm = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF0F766E),
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "মাল পাওয়ার কনফার্মেশন",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "কোম্পানি: ${order.companyName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "অর্ডার পরিমাণ: ${BengaliUtils.formatTaka(order.totalAmount)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    if (order.note.isNotBlank()) {
                        Text(
                            text = "নোট: ${order.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        androidx.compose.material3.Checkbox(
                            checked = recordAsPurchase,
                            onCheckedChange = { recordAsPurchase = it }
                        )
                        Text(
                            text = "আজকের খরচে/মালের ক্রয়ে ৳ ${BengaliUtils.toBanglaDigits(order.totalAmount.toLong())} স্বয়ংক্রিয় যোগ করুন",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.confirmCompanyOrder(order, recordAsPurchase)
                        Toast.makeText(context, "অর্ডার সফলভাবে কনফার্ম ও মাল রিসিভড হয়েছে!", Toast.LENGTH_SHORT).show()
                        orderToConfirm = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
                ) {
                    Text("হ্যাঁ, কনফার্ম করুন", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToConfirm = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Dialog: Delete Order
    orderToDelete?.let { order ->
        AlertDialog(
            onDismissRequest = { orderToDelete = null },
            title = { Text("অর্ডার ডিলিট করবেন?") },
            text = { Text("${order.companyName}-এর এই অর্ডার খাতার রেকর্ডটি সম্পূর্ণ মুছে ফেলা হবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCompanyOrder(order.id)
                        Toast.makeText(context, "অর্ডার মুছে ফেলা হয়েছে", Toast.LENGTH_SHORT).show()
                        orderToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("ডিলিট করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { orderToDelete = null }) {
                    Text("ফিরে যান")
                }
            }
        )
    }

    // Dialog: Add New Company Order
    if (showAddOrderDialog) {
        AddNewCompanyOrderDialog(
            supplierCompanies = supplierCompanies.map { it.companyName },
            onDismiss = { showAddOrderDialog = false },
            onSave = { company, amount, note, dateStr ->
                viewModel.addCompanyOrder(
                    companyName = company,
                    amount = amount,
                    note = note,
                    dateString = dateStr
                )
                Toast.makeText(context, "নতুন কোম্পানি অর্ডার সেভ হয়েছে", Toast.LENGTH_SHORT).show()
                showAddOrderDialog = false
            }
        )
    }
}

/**
 * Company Order Card Item matching Screenshot_2026_1021_021801.png
 */
@Composable
private fun CompanyOrderCardItem(
    order: CompanyOrderEntity,
    onConfirmClick: () -> Unit,
    onCancelClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = Color.Black.copy(alpha = 0.05f)
            )
            .testTag("card_company_order_${order.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDark) MaterialTheme.colorScheme.surface else Color(0xFFFBFDFC)
        ),
        border = BorderStroke(
            1.2.dp,
            if (order.status == "CONFIRMED") Color(0xFF10B981).copy(alpha = 0.4f)
            else Color(0xFFD6EAE0)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Column: Company, Amount, Date, Note
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = order.companyName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 17.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "পরিমাণ মোট: ${BengaliUtils.formatTaka(order.totalAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 14.sp
                )

                Spacer(modifier = Modifier.height(2.dp))

                val formattedDate = remember(order.orderDate, order.orderDateString) {
                    if (order.orderDateString.isNotBlank()) order.orderDateString
                    else {
                        try {
                            val sdf = SimpleDateFormat("d MMMM", Locale("bn", "BD"))
                            sdf.format(Date(order.orderDate))
                        } catch (_: Exception) {
                            com.example.mawa.util.BengaliUtils.getFormattedTodayDate(order.orderDate)
                        }
                    }
                }

                Text(
                    text = "তারিখ: $formattedDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )

                if (order.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "নোট: ${order.note}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Right Action: [ ✓ কনফার্ম করুন \n মাল পেয়েছি ] Button or Confirmed Status
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                when (order.status) {
                    "PENDING" -> {
                        Button(
                            onClick = onConfirmClick,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF0F766E)
                            ),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(
                                horizontal = 14.dp,
                                vertical = 8.dp
                            ),
                            modifier = Modifier
                                .height(52.dp)
                                .shadow(4.dp, RoundedCornerShape(14.dp))
                                .testTag("btn_confirm_order_${order.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White.copy(alpha = 0.25f),
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.Start) {
                                    Text(
                                        text = "কনফার্ম করুন",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "মাল পেয়েছি",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.White.copy(alpha = 0.95f),
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                    "CONFIRMED" -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFE6F4EA),
                            border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF0F766E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "মাল গ্রহণ সম্পন্ন",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                    else -> {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        ) {
                            Text(
                                text = "বাতিলকৃত",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Small overflow menu for cancel / delete
                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "অপশন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        if (order.status == "PENDING") {
                            DropdownMenuItem(
                                text = { Text("অর্ডার বাতিল করুন") },
                                onClick = {
                                    showMenu = false
                                    onCancelClick()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("মুছে ফেলুন (ডিলিট)", color = MaterialTheme.colorScheme.error) },
                            onClick = {
                                showMenu = false
                                onDeleteClick()
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Dialog to add a new company order note
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNewCompanyOrderDialog(
    supplierCompanies: List<String>,
    onDismiss: () -> Unit,
    onSave: (company: String, amount: Double, note: String, dateString: String) -> Unit
) {
    var companyName by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var dateString by remember {
        mutableStateOf(
            try {
                val sdf = SimpleDateFormat("d MMMM", Locale("bn", "BD"))
                sdf.format(Date())
            } catch (_: Exception) {
                com.example.mawa.util.BengaliUtils.getFormattedTodayDate(System.currentTimeMillis())
            }
        )
    }

    val popularCompanies = remember {
        listOf("প্রাণ ফুডস", "মেঘনা গ্রুপ", "আকিজ বেকারি", "বসুন্ধরা গ্রুপ", "স্কয়ার কনজিউমার", "টিপ টপ", "ফ্রেশ")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "নতুন কোম্পানি অর্ডার নোট",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Company Name input
                OutlinedTextField(
                    value = companyName,
                    onValueChange = { companyName = it },
                    label = { Text("কোম্পানির নাম *") },
                    placeholder = { Text("যেমন: প্রাণ ফুডস, মেঘনা গ্রুপ") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick select chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularCompanies.take(3).forEach { c ->
                        Surface(
                            onClick = { companyName = c },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = c,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }

                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { input ->
                        amountText = input.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("মোট অর্ডারের টাকা (৳) *") },
                    placeholder = { Text("যেমন: ৭৫০০") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Date string
                OutlinedTextField(
                    value = dateString,
                    onValueChange = { dateString = it },
                    label = { Text("অর্ডারের তারিখ") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Note input
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("নোট / বিবরণ (যেমন: কম স্টক)") },
                    placeholder = { Text("যেমন: কম স্টক, বিশেষ মাল") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Note chips
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf("কম স্টক", "বিশেষ মাল", "জরুরী অর্ডার").forEach { n ->
                        Surface(
                            onClick = { note = n },
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = n,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = amountText.toDoubleOrNull() ?: 0.0
                    if (companyName.isNotBlank() && amount > 0) {
                        onSave(companyName.trim(), amount, note.trim(), dateString.trim())
                    }
                },
                enabled = companyName.isNotBlank() && (amountText.toDoubleOrNull() ?: 0.0) > 0,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F766E))
            ) {
                Text("অর্ডার সেভ করুন", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}

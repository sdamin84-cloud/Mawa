package com.example.mawa.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BluetoothConnected
import androidx.compose.material.icons.filled.BluetoothDisabled
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.mawa.util.BluetoothPrinterDevice
import com.example.mawa.util.BluetoothThermalPrinterManager
import com.example.mawa.util.CashChimePlayer
import com.example.mawa.util.InvoiceItem
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary
import kotlinx.coroutines.launch

@Composable
fun BluetoothThermalPrinterDialog(
    shopName: String,
    shopPhone: String,
    memoNo: String? = null,
    dateFormatted: String? = null,
    customerName: String? = null,
    items: List<InvoiceItem>? = null,
    totalAmount: Double = 0.0,
    paidAmount: Double = 0.0,
    dueAmount: Double = 0.0,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var pairedDevices by remember { mutableStateOf<List<BluetoothPrinterDevice>>(emptyList()) }
    var selectedDeviceAddress by remember { mutableStateOf<String?>(null) }
    var selectedDeviceName by remember { mutableStateOf<String?>(null) }
    var is80mm by remember { mutableStateOf(false) }
    var isPrinting by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isSuccess by remember { mutableStateOf(false) }

    // Check permission launcher for Android 12+
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms.values.all { it }
        if (granted) {
            pairedDevices = BluetoothThermalPrinterManager.getPairedPrinters(context)
        } else {
            Toast.makeText(context, "ব্লুটুথ পারমিশন প্রয়োজন", Toast.LENGTH_SHORT).show()
        }
    }

    fun checkAndLoadDevices() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val connectGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_CONNECT
            ) == PackageManager.PERMISSION_GRANTED
            val scanGranted = ContextCompat.checkSelfPermission(
                context, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED

            if (!connectGranted || !scanGranted) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.BLUETOOTH_CONNECT,
                        Manifest.permission.BLUETOOTH_SCAN
                    )
                )
                return
            }
        }
        pairedDevices = BluetoothThermalPrinterManager.getPairedPrinters(context)
        val (prefAddr, prefName) = BluetoothThermalPrinterManager.getPreferredPrinter(context)
        if (prefAddr != null) {
            selectedDeviceAddress = prefAddr
            selectedDeviceName = prefName
        } else if (pairedDevices.isNotEmpty()) {
            val firstPrinter = pairedDevices.firstOrNull { it.isLikelyPrinter } ?: pairedDevices.first()
            selectedDeviceAddress = firstPrinter.address
            selectedDeviceName = firstPrinter.name
        }
    }

    LaunchedEffect(Unit) {
        checkAndLoadDevices()
    }

    fun executePrint(isTest: Boolean = false) {
        val targetAddr = selectedDeviceAddress
        if (targetAddr == null) {
            Toast.makeText(context, "একটি প্রিন্টার সিলেক্ট করুন", Toast.LENGTH_SHORT).show()
            return
        }

        isPrinting = true
        statusMessage = "প্রিন্টারে কানেক্ট হচ্ছে..."
        isSuccess = false

        scope.launch {
            val payload = if (isTest || items == null || memoNo == null || dateFormatted == null) {
                BluetoothThermalPrinterManager.buildTestPrintEscPos(shopName)
            } else {
                BluetoothThermalPrinterManager.buildThermalReceiptEscPos(
                    shopName = shopName,
                    shopPhone = shopPhone,
                    memoNo = memoNo,
                    dateFormatted = dateFormatted,
                    customerName = customerName,
                    items = items,
                    totalAmount = totalAmount,
                    paidAmount = paidAmount,
                    dueAmount = dueAmount,
                    is80mm = is80mm
                )
            }

            val result = BluetoothThermalPrinterManager.sendBytesToPrinter(context, targetAddr, payload)
            isPrinting = false
            if (result.isSuccess) {
                isSuccess = true
                statusMessage = "প্রিন্ট সফলভাবে সম্পন্ন হয়েছে!"
                CashChimePlayer.playCashChime(context)
                // Save preferred printer
                selectedDeviceName?.let { name ->
                    BluetoothThermalPrinterManager.setPreferredPrinter(context, targetAddr, name)
                }
            } else {
                isSuccess = false
                statusMessage = result.exceptionOrNull()?.message ?: "প্রিন্ট করতে সমস্যা হয়েছে"
            }
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .testTag("dialog_bluetooth_printer"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(MawaPrimary.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Print,
                                contentDescription = null,
                                tint = MawaPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "থার্মাল প্রিন্টার (POS)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "ব্লুটুথ পিওএস প্রিন্টারে রসিদ",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", modifier = Modifier.size(18.dp))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(12.dp))

                // Paper width toggle (58mm vs 80mm)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কাগজের সাইজ:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { is80mm = false }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            color = if (!is80mm) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "৫৮ মিমি (২ ইঞ্চি)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!is80mm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { is80mm = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            color = if (is80mm) MawaPrimary else MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = "৮০ মিমি (৩ ইঞ্চি)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (is80mm) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Device Selection Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "কানেক্টেড / পেয়ার করা ডিভাইস:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    IconButton(
                        onClick = { checkAndLoadDevices() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = "রিফ্রেশ", modifier = Modifier.size(16.dp))
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Device List
                if (pairedDevices.isEmpty()) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.BluetoothDisabled,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.outline,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "কোনো পেয়ার করা ব্লুটুথ ডিভাইস পাওয়া যায়নি",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
                                    context.startActivity(intent)
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ফোনের ব্লুটুথ সেটিংসে যান", fontSize = 11.sp)
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                    ) {
                        items(pairedDevices) { device ->
                            val isSelected = device.address == selectedDeviceAddress
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedDeviceAddress = device.address
                                        selectedDeviceName = device.name
                                    },
                                color = if (isSelected) MawaPrimary.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                border = BorderStroke(
                                    width = if (isSelected) 1.5.dp else 0.5.dp,
                                    color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = if (device.isLikelyPrinter) Icons.Default.Print else Icons.Default.Bluetooth,
                                            contentDescription = null,
                                            tint = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = device.name,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                if (device.isLikelyPrinter) {
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Surface(
                                                        color = FinancialPositive.copy(alpha = 0.15f),
                                                        shape = RoundedCornerShape(4.dp)
                                                    ) {
                                                        Text(
                                                            text = "POS",
                                                            fontSize = 9.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = FinancialPositive,
                                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                        )
                                                    }
                                                }
                                            }
                                            Text(
                                                text = device.address,
                                                fontSize = 10.sp,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                            )
                                        }
                                    }

                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "নির্বাচিত",
                                            tint = MawaPrimary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Status message
                AnimatedVisibility(visible = statusMessage != null) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = if (isSuccess) FinancialPositive.copy(alpha = 0.12f) else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isPrinting) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = statusMessage ?: "",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSuccess) FinancialPositive else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Bottom Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { executePrint(isTest = true) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        enabled = !isPrinting && selectedDeviceAddress != null
                    ) {
                        Text("টেস্ট প্রিন্ট", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { executePrint(isTest = false) },
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("btn_confirm_bluetooth_print"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                        enabled = !isPrinting && selectedDeviceAddress != null
                    ) {
                        if (isPrinting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(6.dp))
                        } else {
                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Text(
                            text = if (items != null) "রসিদ প্রিন্ট করুন" else "প্রিন্ট শুরু করুন",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

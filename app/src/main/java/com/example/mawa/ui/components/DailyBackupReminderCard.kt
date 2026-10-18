package com.example.mawa.ui.components

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.mawa.data.local.entity.CustomerEntity
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.PersonalTransactionEntity
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.ShopSettingsEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.util.CashChimePlayer
import com.example.mawa.util.DataBackupRestoreManager
import com.example.mawa.util.FullBackupData
import com.example.mawa.util.ReportExportUtils
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DailyBackupReminderCard(
    allTransactions: List<TransactionEntity>,
    allCustomers: List<CustomerEntity>,
    allProducts: List<ProductEntity>,
    allFordiItems: List<FordiItemEntity>,
    allSupplierCompanies: List<SupplierCompanyEntity>,
    personalTransactions: List<PersonalTransactionEntity> = emptyList(),
    shopSettings: ShopSettingsEntity?,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember { context.getSharedPreferences("mawa_app_prefs", Context.MODE_PRIVATE) }

    val todayDateStr = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date()) }
    var lastBackupDate by remember { mutableStateOf(prefs.getString("last_backup_date", "")) }
    var isDismissed by remember { mutableStateOf(false) }
    var isBackingUp by remember { mutableStateOf(false) }

    val isBackupDoneToday = lastBackupDate == todayDateStr

    if (isDismissed || isBackupDoneToday) return

    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    val cardBg = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
    val cardBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
    val amberAccent = if (isDark) Color(0xFFFBBF24) else Color(0xFFD97706)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("card_daily_backup_reminder"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(amberAccent.copy(alpha = if (isDark) 0.2f else 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = amberAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "দৈনিক ডাটা ব্যাকআপ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Excel / CSV Backup
                    OutlinedButton(
                        onClick = {
                            isBackingUp = true
                            scope.launch {
                                try {
                                    val csv = ReportExportUtils.generateTransactionsCsv(allTransactions, "আজকের হিসাব")
                                    shareDailyBackupFile(context, csv, "Mawa_Daily_Excel_$todayDateStr.csv", "text/csv")
                                    prefs.edit().putString("last_backup_date", todayDateStr).apply()
                                    lastBackupDate = todayDateStr
                                    CashChimePlayer.playCashChime(context)
                                    Toast.makeText(context, "এক্সেল ব্যাকআপ ফাইল প্রস্তুত!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isBackingUp = false
                                }
                            }
                        },
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(13.dp), tint = amberAccent)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("এক্সেল", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                    }

                    // Full JSON Database Backup
                    Button(
                        onClick = {
                            isBackingUp = true
                            scope.launch {
                                try {
                                    val backupData = FullBackupData(
                                        shopSettings = shopSettings,
                                        customers = allCustomers,
                                        transactions = allTransactions,
                                        fordiItems = allFordiItems,
                                        products = allProducts,
                                        supplierCompanies = allSupplierCompanies,
                                        personalTransactions = personalTransactions
                                    )
                                    val jsonString = DataBackupRestoreManager.exportToJsonString(backupData)
                                    shareDailyBackupFile(context, jsonString, "Mawa_Full_Backup_$todayDateStr.json", "application/json")
                                    prefs.edit().putString("last_backup_date", todayDateStr).apply()
                                    lastBackupDate = todayDateStr
                                    CashChimePlayer.playCashChime(context)
                                    Toast.makeText(context, "ডাটাবেস ব্যাকআপ সম্পন্ন!", Toast.LENGTH_SHORT).show()
                                } catch (e: Exception) {
                                    Toast.makeText(context, "সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
                                } finally {
                                    isBackingUp = false
                                }
                            }
                        },
                        modifier = Modifier
                            .height(30.dp)
                            .testTag("btn_daily_backup_now"),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(6.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = amberAccent)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(13.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ব্যাকআপ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    IconButton(
                        onClick = { isDismissed = true },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "বন্ধ করুন",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun shareDailyBackupFile(context: Context, content: String, fileName: String, mimeType: String) {
    val file = File(context.cacheDir, fileName)
    file.writeText(content, Charsets.UTF_8)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, "MAWA হিসাব ব্যাকআপ $fileName")
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    val chooser = Intent.createChooser(intent, "ব্যাকআপ ফাইল সংরক্ষণ বা শেয়ার করুন")
    chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    context.startActivity(chooser)
}

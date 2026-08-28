package com.example.mawa.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mawa.data.remote.supabase.CloudBackupItem
import com.example.mawa.data.remote.supabase.SupabaseConfig
import com.example.mawa.ui.viewmodel.MawaViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SupabaseCloudDialog(
    viewModel: MawaViewModel,
    onDismiss: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val isAuthLoading by viewModel.isAuthLoading.collectAsState()
    val isSyncing by viewModel.isCloudSyncing.collectAsState()
    val cloudBackups by viewModel.cloudBackups.collectAsState()

    var selectedTab by remember { mutableIntStateOf(if (currentUser != null) 0 else 0) }

    // Dialogs inside
    var showDeleteConfirmBackup by remember { mutableStateOf<CloudBackupItem?>(null) }
    var showRestoreConfirmBackup by remember { mutableStateOf<CloudBackupItem?>(null) }
    var actionStatusMessage by remember { mutableStateOf<String?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .heightIn(max = 680.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF3ECF8E).copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudSync,
                                contentDescription = null,
                                tint = Color(0xFF10B981)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "সুপাবেজ ক্লাউড সিঙ্ক",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "সুপাবেজে সম্পূর্ণ নিরাপদ ব্যাকআপ ও পুনরুদ্ধার",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Text(
                            text = "✕",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // User state chip / status
                if (currentUser != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = Color(0xFF10B981),
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = currentUser?.displayName?.ifBlank { "ব্যবহারকারী" } ?: "ব্যবহারকারী",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = currentUser?.email ?: "",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = { viewModel.logoutSupabase() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.ExitToApp, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("লগআউট", fontSize = 12.sp)
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ক্লাউড ব্যাকআপ ও রিস্টোর করতে অনুগ্রহ করে লগইন করুন।",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Tab Switcher
                if (currentUser != null) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = Color(0xFF10B981),
                        indicator = { tabPositions ->
                            TabRowDefaults.Indicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF10B981)
                            )
                        }
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("ক্লাউড ব্যাকআপ ও রিস্টোর", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("টেবিল সিঙ্ক ও প্রোফাইল", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content body
                if (currentUser == null) {
                    SupabaseAuthSection(
                        isLoading = isAuthLoading,
                        onLogin = { email, pass ->
                            viewModel.loginWithSupabase(email, pass) { success, msg ->
                                actionStatusMessage = msg
                            }
                        },
                        onRegister = { email, pass, name ->
                            viewModel.registerWithSupabase(email, pass, name) { success, msg ->
                                actionStatusMessage = msg
                            }
                        },
                        onResetPassword = { email ->
                            viewModel.resetSupabasePassword(email) { success, msg ->
                                actionStatusMessage = msg
                            }
                        }
                    )
                } else {
                    if (selectedTab == 0) {
                        SupabaseBackupRestoreSection(
                            cloudBackups = cloudBackups,
                            isSyncing = isSyncing,
                            onUploadBackup = { customName ->
                                viewModel.uploadBackupToSupabase(customName) { success, msg ->
                                    actionStatusMessage = msg
                                }
                            },
                            onRefreshBackups = { viewModel.loadCloudBackups() },
                            onRestoreClick = { showRestoreConfirmBackup = it },
                            onDeleteClick = { showDeleteConfirmBackup = it }
                        )
                    } else {
                        SupabaseTableSyncSection(
                            currentUser = currentUser!!,
                            isSyncing = isSyncing,
                            onSyncAll = {
                                viewModel.syncAllLocalRecordsToSupabase { success, msg ->
                                    actionStatusMessage = msg
                                }
                            }
                        )
                    }
                }

                if (actionStatusMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = actionStatusMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF10B981),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }

    // Confirmation dialogs
    showDeleteConfirmBackup?.let { item ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmBackup = null },
            title = { Text("ক্লাউড ব্যাকআপ মুছে ফেলবেন?") },
            text = { Text("'${item.backupName}' ব্যাকআপটি সুপাবেজ ক্লাউড থেকে স্থায়ীভাবে মুছে যাবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCloudBackup(item.id) { _, _ -> }
                        showDeleteConfirmBackup = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("মুছে ফেলুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmBackup = null }) {
                    Text("বাতিল")
                }
            }
        )
    }

    showRestoreConfirmBackup?.let { item ->
        AlertDialog(
            onDismissRequest = { showRestoreConfirmBackup = null },
            title = { Text("ক্লাউড থেকে রিস্টোর করবেন?") },
            text = { Text("'${item.backupName}' ব্যাকআপ থেকে সকল দোকান, কাস্টমার, বাকি ও খরচের হিসাব পুনরায় ফোনে প্রতিস্থাপিত হবে।") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreCloudBackup(item) { _, _ -> }
                        showRestoreConfirmBackup = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("রিস্টোর করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmBackup = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
fun SupabaseAuthSection(
    isLoading: Boolean,
    onLogin: (email: String, pass: String) -> Unit,
    onRegister: (email: String, pass: String, name: String) -> Unit,
    onResetPassword: (email: String) -> Unit
) {
    var mode by remember { mutableIntStateOf(0) } // 0: Login, 1: Register, 2: Reset Password
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Mode selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            TextButton(
                onClick = { mode = 0 },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (mode == 0) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("লগইন", fontWeight = if (mode == 0) FontWeight.Bold else FontWeight.Normal)
            }
            Text(" • ", modifier = Modifier.align(Alignment.CenterVertically), color = Color.Gray)
            TextButton(
                onClick = { mode = 1 },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (mode == 1) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("নতুন অ্যাকাউন্ট", fontWeight = if (mode == 1) FontWeight.Bold else FontWeight.Normal)
            }
            Text(" • ", modifier = Modifier.align(Alignment.CenterVertically), color = Color.Gray)
            TextButton(
                onClick = { mode = 2 },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = if (mode == 2) Color(0xFF10B981) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            ) {
                Text("পাসওয়ার্ড রিসেট", fontWeight = if (mode == 2) FontWeight.Bold else FontWeight.Normal)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Name field for registration
        if (mode == 1) {
            OutlinedTextField(
                value = displayName,
                onValueChange = { displayName = it },
                label = { Text("দোকান / আপনার নাম") },
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }

        // Email field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("ইমেইল অ্যাড্রেস") },
            placeholder = { Text("যেমন: yourshop@gmail.com") },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = if (mode == 2) ImeAction.Done else ImeAction.Next),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(12.dp)
        )

        // Password field (for login & register)
        if (mode != 2) {
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("পাসওয়ার্ড (কমপক্ষে ৬ অক্ষর)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                        Icon(
                            imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Submit Button
        Button(
            onClick = {
                focusManager.clearFocus()
                when (mode) {
                    0 -> onLogin(email, password)
                    1 -> onRegister(email, password, displayName)
                    2 -> onResetPassword(email)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            enabled = !isLoading && email.isNotBlank() && (mode == 2 || password.isNotBlank()),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("অপেক্ষা করুন...")
            } else {
                when (mode) {
                    0 -> {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সুপাবেজে লগইন করুন", fontWeight = FontWeight.Bold)
                    }
                    1 -> {
                        Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("অ্যাকাউন্ট তৈরি করুন", fontWeight = FontWeight.Bold)
                    }
                    2 -> {
                        Icon(Icons.Default.LockReset, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("রিসেট লিংক পাঠান", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SupabaseBackupRestoreSection(
    cloudBackups: List<CloudBackupItem>,
    isSyncing: Boolean,
    onUploadBackup: (name: String) -> Unit,
    onRefreshBackups: () -> Unit,
    onRestoreClick: (CloudBackupItem) -> Unit,
    onDeleteClick: (CloudBackupItem) -> Unit
) {
    var showCustomNameInput by remember { mutableStateOf(false) }
    var backupCustomName by remember {
        mutableStateOf("MAWA_BACKUP_${SimpleDateFormat("ddMMMyyyy_HHmm", Locale.US).format(Date())}")
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    ) {
        // Cloud Backup Upload action card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3ECF8E).copy(alpha = 0.08f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "ক্লাউডে সম্পূর্ণ ব্যাকআপ রাখুন",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color(0xFF047857)
                        )
                        Text(
                            text = "বর্তমান দোকান, কাস্টমার, বাকি ও খরচের ডাটা Supabase user_backups টেবিলে জমা হবে",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = {
                            if (!showCustomNameInput) {
                                onUploadBackup(backupCustomName)
                            } else {
                                onUploadBackup(backupCustomName)
                                showCustomNameInput = false
                            }
                        },
                        enabled = !isSyncing,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        if (isSyncing) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("আপলোড")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = { showCustomNameInput = !showCustomNameInput }) {
                        Text(
                            text = if (showCustomNameInput) "স্বয়ংক্রিয় নাম ব্যবহার করুন" else "কাস্টম ব্যাকআপের নাম দিন ✎",
                            fontSize = 12.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                }

                if (showCustomNameInput) {
                    OutlinedTextField(
                        value = backupCustomName,
                        onValueChange = { backupCustomName = it },
                        label = { Text("ব্যাকআপের নাম") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Cloud backups list header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "সংরক্ষিত ক্লাউড ব্যাকআপ (${cloudBackups.size})",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            IconButton(onClick = onRefreshBackups, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Refresh, contentDescription = "রিফ্রেশ", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (cloudBackups.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "এখনও কোনো ক্লাউড ব্যাকআপ তৈরি করা হয়নি",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "উপরের 'আপলোড' বাটনে চাপ দিয়ে ব্যাকআপ রাখুন",
                        fontSize = 11.sp,
                        color = Color(0xFF10B981)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 240.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cloudBackups, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.backupName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                Text(
                                    text = "আইডি #${item.id} • ${item.updatedAt.take(19)}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = { onRestoreClick(item) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CloudDownload,
                                        contentDescription = "রিস্টোর",
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onDeleteClick(item) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "মুছুন",
                                        tint = Color(0xFFEF4444),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SupabaseTableSyncSection(
    currentUser: com.example.mawa.data.remote.supabase.SupabaseUser,
    isSyncing: Boolean,
    onSyncAll: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
    ) {
        // Table info cards
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "সুপাবেজ ডাটাবেজ কনফিগারেশন",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• হোস্ট: ${SupabaseConfig.SUPABASE_URL}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• ব্যাকআপ টেবিল: ${SupabaseConfig.TABLE_USER_BACKUPS}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• ক্লাউড রেকর্ড টেবিল: ${SupabaseConfig.TABLE_CLOUD_RECORDS}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• ইউজার আইডি (UID): ${currentUser.id}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Direct record sync
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF3ECF8E).copy(alpha = 0.1f)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "সরাসরি রেকর্ড সিঙ্ক (mawa_cloud_records)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color(0xFF047857)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "আপনার সকল লেনদেন, কাস্টমার, ফর্দ ও খরচের আইটেম সরাসরি সুপাবেজের mawa_cloud_records টেবিলে পৃথক রো হিসেবে পুশ করা হবে।",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onSyncAll,
                    enabled = !isSyncing,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("ক্লাউডে সিঙ্ক হচ্ছে...")
                    } else {
                        Icon(Icons.Default.CloudSync, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("এখনই সম্পূর্ণ ডাটা সিঙ্ক করুন")
                    }
                }
            }
        }
    }
}

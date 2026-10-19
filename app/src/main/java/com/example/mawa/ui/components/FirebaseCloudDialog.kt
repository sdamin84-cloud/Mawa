package com.example.mawa.ui.components

import android.app.Activity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mawa.data.remote.firebase.FirebaseBackupItem
import com.example.mawa.data.remote.firebase.FirebaseUserProfile
import com.example.mawa.ui.viewmodel.MawaViewModel
import com.example.mawa.util.BengaliUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseCloudDialog(
    viewModel: MawaViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentUser by viewModel.currentFirebaseUser.collectAsState()
    val isAuthLoading by viewModel.isFirebaseAuthLoading.collectAsState()
    val isSyncing by viewModel.isFirebaseSyncing.collectAsState()
    val backups by viewModel.firebaseBackups.collectAsState()

    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }
    var showBackupNamingDialog by remember { mutableStateOf(false) }
    var selectedBackupForRestore by remember { mutableStateOf<FirebaseBackupItem?>(null) }
    var showEmailAuthFields by remember { mutableStateOf(false) }
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp)),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFF9100).copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.CloudSync,
                                    contentDescription = "Firebase Cloud",
                                    tint = Color(0xFFFF9100),
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "গুগল ফায়ারবেস ক্লাউড",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 19.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Google Cloud Firestore সুরক্ষিত ব্যাকআপ",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Status Message Banner
                statusMessage?.let { msg ->
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isErrorMessage) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isErrorMessage) Icons.Default.ErrorOutline else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isErrorMessage) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                color = if (isErrorMessage) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 1. User Authentication Section
                    item {
                        if (currentUser != null) {
                            val user = currentUser!!
                            FirebaseUserCard(
                                user = user,
                                onSignOut = {
                                    viewModel.signOutFirebase()
                                    statusMessage = "ফায়ারবেস থেকে সাইন-আউট করা হয়েছে"
                                    isErrorMessage = false
                                }
                            )
                        } else {
                            FirebaseSignInCard(
                                isLoading = isAuthLoading,
                                onGoogleSignIn = {
                                    statusMessage = null
                                    viewModel.signInFirebaseWithGoogle(context) { success, msg ->
                                        statusMessage = msg
                                        isErrorMessage = !success
                                    }
                                },
                                showEmailAuth = showEmailAuthFields,
                                onToggleEmailAuth = { showEmailAuthFields = !showEmailAuthFields },
                                email = emailInput,
                                onEmailChange = { emailInput = it },
                                password = passwordInput,
                                onPasswordChange = { passwordInput = it },
                                name = nameInput,
                                onNameChange = { nameInput = it },
                                isRegister = isRegisterMode,
                                onToggleRegister = { isRegisterMode = !isRegisterMode },
                                onEmailSubmit = {
                                    if (emailInput.isBlank() || passwordInput.isBlank()) {
                                        statusMessage = "ইমেইল ও পাসওয়ার্ড প্রদান করুন"
                                        isErrorMessage = true
                                        return@FirebaseSignInCard
                                    }
                                    if (isRegisterMode) {
                                        viewModel.signUpFirebaseWithEmail(emailInput, passwordInput, nameInput) { success, msg ->
                                            statusMessage = msg
                                            isErrorMessage = !success
                                        }
                                    } else {
                                        viewModel.signInFirebaseWithEmail(emailInput, passwordInput) { success, msg ->
                                            statusMessage = msg
                                            isErrorMessage = !success
                                        }
                                    }
                                }
                            )
                        }
                    }

                    // 2. Cloud Backup Actions (when logged in)
                    if (currentUser != null) {
                        item {
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                                )
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudUpload,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "ক্লাউড ব্যাকআপ তৈরি করুন",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }

                                    Text(
                                        text = "আপনার সমস্ত কাস্টমার, বাকি, লেনদেন, পণ্য ও ক্যাশ খাতার তথ্য সরাসরি আপনার সুরক্ষিত ফায়ারবেস ক্লাউডে সংরক্ষণ করুন। মোবাইল হারালেও যেকোনো সময় পুনরুদ্ধার করা যাবে।",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    Button(
                                        onClick = { showBackupNamingDialog = true },
                                        enabled = !isSyncing,
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        )
                                    ) {
                                        if (isSyncing) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(18.dp),
                                                strokeWidth = 2.dp,
                                                color = MaterialTheme.colorScheme.onPrimary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("ব্যাকআপ আপলোড হচ্ছে...")
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CloudUpload,
                                                contentDescription = null,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("এখনই ফায়ারবেসে ব্যাকআপ নিন", fontWeight = FontWeight.SemiBold)
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Saved Backups List Header
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "সংরক্ষিত ক্লাউড ব্যাকআপসমূহ (${BengaliUtils.toBengaliDigits(backups.size.toLong())})",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                                    )
                                }

                                TextButton(
                                    onClick = { viewModel.loadFirebaseBackups() },
                                    enabled = !isSyncing
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Refresh",
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("রিফ্রেশ", fontSize = 13.sp)
                                }
                            }
                        }

                        if (backups.isEmpty()) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CloudOff,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Text(
                                            text = "কোনো ক্লাউড ব্যাকআপ পাওয়া যায়নি",
                                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "উপরের বাটন চাপ দিয়ে প্রথম ব্যাকআপ সংরক্ষণ করুন।",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        } else {
                            items(backups, key = { it.id }) { item ->
                                FirebaseBackupCardItem(
                                    item = item,
                                    isSyncing = isSyncing,
                                    onRestore = { selectedBackupForRestore = item },
                                    onDelete = {
                                        viewModel.deleteFirebaseBackup(item.id) { success, msg ->
                                            statusMessage = msg
                                            isErrorMessage = !success
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog for naming new backup
    if (showBackupNamingDialog) {
        val defaultName = remember {
            val df = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            "মাওয়া ক্লাউড - ${df.format(Date())}"
        }
        var backupName by remember { mutableStateOf(defaultName) }

        AlertDialog(
            onDismissRequest = { showBackupNamingDialog = false },
            title = {
                Text("ক্লাউড ব্যাকআপের নাম দিন", fontWeight = FontWeight.Bold)
            },
            text = {
                OutlinedTextField(
                    value = backupName,
                    onValueChange = { backupName = it },
                    label = { Text("ব্যাকআপ লেবেল") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showBackupNamingDialog = false
                        viewModel.uploadBackupToFirebase(backupName) { success, msg ->
                            statusMessage = msg
                            isErrorMessage = !success
                        }
                    }
                ) {
                    Text("আপলোড করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackupNamingDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    // Confirmation dialog for restore
    selectedBackupForRestore?.let { backupItem ->
        AlertDialog(
            onDismissRequest = { selectedBackupForRestore = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(32.dp)
                )
            },
            title = {
                Text("ব্যাকআপ রিস্টোর নিশ্চিত করুন", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "আপনি কি '${backupItem.backupName}' ব্যাকআপটি রিস্টোর করতে চান? এতে বর্তমান খাতার হিসাব উক্ত ব্যাকআপের তথ্যের সাথে সমন্বয় হবে।",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val item = backupItem
                        selectedBackupForRestore = null
                        viewModel.restoreFirebaseBackup(item) { success, msg ->
                            statusMessage = msg
                            isErrorMessage = !success
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("হ্যাঁ, রিস্টোর করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedBackupForRestore = null }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

@Composable
private fun FirebaseUserCard(
    user: FirebaseUserProfile,
    onSignOut: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = (user.displayName.ifBlank { user.email }).take(1).uppercase(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = user.displayName.ifBlank { "Google ব্যবহারকারী" },
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF4CAF50).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "সংযুক্ত",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                ),
                                color = Color(0xFF2E7D32),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (user.email.isNotBlank()) {
                        Text(
                            text = user.email,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                OutlinedButton(
                    onClick = onSignOut,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Sign out",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("লগআউট", fontSize = 12.sp)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ফায়ারবেস ক্লাউড ডাটাবেস:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Google Cloud Firestore",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun FirebaseSignInCard(
    isLoading: Boolean,
    onGoogleSignIn: () -> Unit,
    showEmailAuth: Boolean,
    onToggleEmailAuth: () -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    name: String,
    onNameChange: (String) -> Unit,
    isRegister: Boolean,
    onToggleRegister: () -> Unit,
    onEmailSubmit: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = Color(0xFFFF9100).copy(alpha = 0.15f),
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Cloud,
                        contentDescription = null,
                        tint = Color(0xFFFF9100),
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Text(
                text = "গুগল ক্লাউড সাইন-ইন",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center
            )

            Text(
                text = "আপনার গুগল অ্যাকাউন্ট দিয়ে সাইন-ইন করে ক্লাউড ব্যাকআপ সক্রিয় করুন। মোবাইল নষ্ট বা চুরি হলেও আপনার ব্যবসার একটি টাকাও হারাবে না।",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            // One-tap Google Sign-In Button
            Button(
                onClick = onGoogleSignIn,
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4285F4)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("সাইন-ইন হচ্ছে...", color = Color.White)
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = "Google",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Google দিয়ে এক ক্লিকে সাইন-ইন করুন",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            TextButton(onClick = onToggleEmailAuth) {
                Text(
                    text = if (showEmailAuth) "ইমেইল লগইন লুকান ▲" else "অথবা ইমেইল ও পাসওয়ার্ড দিয়ে প্রবেশ করুন ▼",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            AnimatedVisibility(visible = showEmailAuth) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isRegister) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = onNameChange,
                            label = { Text("আপনার নাম / দোকানের নাম") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    OutlinedTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = { Text("ইমেইল অ্যাড্রেস") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = { Text("পাসওয়ার্ড") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = onEmailSubmit,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isRegister) "রেজিস্ট্রেশন করুন" else "লগইন করুন")
                    }

                    TextButton(
                        onClick = onToggleRegister,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Text(
                            text = if (isRegister) "পূর্বে অ্যাকাউন্ট থাকলে লগইন করুন" else "নতুন অ্যাকাউন্ট তৈরি করুন",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FirebaseBackupCardItem(
    item: FirebaseBackupItem,
    isSyncing: Boolean,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.backupName,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = item.formattedDate.ifBlank { "তারিখ অজ্ঞাত" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = onDelete,
                    enabled = !isSyncing,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Summary metrics chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricChip(label = "কাস্টমার", value = BengaliUtils.toBengaliDigits(item.customerCount.toLong()))
                MetricChip(label = "লেনদেন", value = BengaliUtils.toBengaliDigits(item.transactionCount.toLong()))
                MetricChip(label = "বাকি", value = BengaliUtils.formatTaka(item.totalBaki))
            }

            Button(
                onClick = onRestore,
                enabled = !isSyncing,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsBackupRestore,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("এই ব্যাকআপ রিস্টোর করুন", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun MetricChip(label: String, value: String) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "$label:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

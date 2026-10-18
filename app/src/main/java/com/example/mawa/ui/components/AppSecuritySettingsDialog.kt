package com.example.mawa.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.util.AppLockManager
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.MawaPrimary

/**
 * AppSecuritySettingsDialog:
 * Allows shopkeepers to enable/disable 4-digit PIN lock, change PIN, set recovery phone,
 * and lock the app on demand.
 */
@Composable
fun AppSecuritySettingsDialog(
    shopPhone: String,
    onDismiss: () -> Unit,
    onLockNow: () -> Unit
) {
    val context = LocalContext.current
    val isInitiallyLocked = remember { AppLockManager.isLockEnabled(context) }
    var isLockEnabled by remember { mutableStateOf(isInitiallyLocked) }

    var isChangingPin by remember { mutableStateOf(!isInitiallyLocked) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var recoveryPhone by remember {
        mutableStateOf(AppLockManager.getRecoveryPhone(context).ifBlank { shopPhone })
    }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "অ্যাপ নিরাপত্তা ও পিন লক",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "দোকানের ক্যাশ ও খাতার গোপনীয়তা রক্ষা",
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
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Info Card
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "পিন লক চালু রাখলে অন্য কেউ অ্যাপ খুললে দোকানের ক্যাশ ব্যালেন্স বা বাকি দেখতে পারবে না।",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 16.sp
                        )
                    }
                }

                // Toggle Switch Row
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "গোপন পিন লক সক্রিয় করুন",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isLockEnabled) "বর্তমানে সক্রিয় রয়েছে" else "নিষ্ক্রিয় আছে",
                                fontSize = 11.sp,
                                color = if (isLockEnabled) FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = isLockEnabled,
                            onCheckedChange = { checked ->
                                if (!checked) {
                                    // Disabling lock
                                    AppLockManager.setLockEnabled(context, false)
                                    isLockEnabled = false
                                    isChangingPin = false
                                    Toast.makeText(context, "পিন লক নিষ্ক্রিয় করা হয়েছে", Toast.LENGTH_SHORT).show()
                                } else {
                                    isLockEnabled = true
                                    isChangingPin = true
                                }
                            },
                            thumbContent = {
                                Icon(
                                    imageVector = if (isLockEnabled) Icons.Default.Check else Icons.Default.LockOpen,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = MawaPrimary
                            ),
                            modifier = Modifier.testTag("switch_app_lock")
                        )
                    }
                }

                // If lock is enabled, allow changing PIN or setting new PIN
                AnimatedVisibility(visible = isLockEnabled) {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)

                        if (isInitiallyLocked && !isChangingPin) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "৪ সংখ্যার গোপন পিন সেট করা আছে",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                OutlinedButton(
                                    onClick = { isChangingPin = true },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("পিন পরিবর্তন", fontSize = 12.sp)
                                }
                            }

                            // Lock Now Button
                            Button(
                                onClick = {
                                    onDismiss()
                                    onLockNow()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("btn_lock_now_action")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("এখনই অ্যাপ লক করুন")
                            }
                        } else {
                            Text(
                                text = if (isInitiallyLocked) "নতুন পিন কোড সেট করুন:" else "প্রথমবার ৪ সংখ্যার পিন দিন:",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            if (isInitiallyLocked) {
                                OutlinedTextField(
                                    value = oldPin,
                                    onValueChange = {
                                        if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                            oldPin = it
                                            errorMessage = ""
                                        }
                                    },
                                    label = { Text("পুরাতন পিন কোড") },
                                    visualTransformation = PasswordVisualTransformation(),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            OutlinedTextField(
                                value = newPin,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                        newPin = it
                                        errorMessage = ""
                                    }
                                },
                                label = { Text("নতুন ৪ সংখ্যার পিন") },
                                placeholder = { Text("যেমন: ১২৩৪") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = confirmPin,
                                onValueChange = {
                                    if (it.length <= 4 && it.all { c -> c.isDigit() }) {
                                        confirmPin = it
                                        errorMessage = ""
                                    }
                                },
                                label = { Text("পিন পুনরায় লিখুন") },
                                visualTransformation = PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            OutlinedTextField(
                                value = recoveryPhone,
                                onValueChange = { recoveryPhone = it },
                                label = { Text("রিকভারি মোবাইল নম্বর (পিন ভুলে গেলে)") },
                                placeholder = { Text("যেমন: ০১৭১২-৩৪৫৬৭৮") },
                                leadingIcon = {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { Text("পিন ভুলে গেলে এই নম্বর দিয়ে তাৎক্ষণিক রিসেট করতে পারবেন") }
                            )

                            if (errorMessage.isNotBlank()) {
                                Text(
                                    text = errorMessage,
                                    color = MaterialTheme.colorScheme.error,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (isLockEnabled && isChangingPin) {
                Button(
                    onClick = {
                        if (isInitiallyLocked) {
                            if (!AppLockManager.verifyPin(context, oldPin)) {
                                errorMessage = "পুরাতন পিন সঠিক নয়!"
                                return@Button
                            }
                        }
                        if (newPin.length != 4) {
                            errorMessage = "পিন অবশ্যই ৪ সংখ্যার হতে হবে"
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            errorMessage = "দুটো পিন এক হয়নি! আবার টাইপ করুন"
                            return@Button
                        }
                        AppLockManager.savePin(
                            context = context,
                            pin = newPin,
                            recoveryPhone = recoveryPhone
                        )
                        Toast.makeText(context, "পিন সফলভাবে সংরক্ষণ করা হয়েছে!", Toast.LENGTH_SHORT).show()
                        isChangingPin = false
                        onDismiss()
                    }
                ) {
                    Text("সংরক্ষণ করুন")
                }
            } else {
                Button(onClick = onDismiss) {
                    Text("ঠিক আছে")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বন্ধ করুন")
            }
        }
    )
}

package com.example.mawa.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ripple
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.util.AppLockManager
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.MawaPrimary
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * AppLockScreen:
 * Fullscreen secure PIN lock keypad to protect shop finance, cash drawer, and credit ledgers.
 */
@Composable
fun AppLockScreen(
    shopName: String?,
    onUnlocked: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showForgotDialog by remember { mutableStateOf(false) }

    // Shake animation on wrong PIN
    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        coroutineScope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            )
        }
    }

    fun onDigitPress(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            isError = false

            if (newPin.length == 4) {
                // Verify PIN
                val success = AppLockManager.verifyPin(context, newPin)
                if (success) {
                    onUnlocked()
                } else {
                    isError = true
                    errorMessage = "ভুল পিন! সঠিক ৪ সংখ্যার পিন দিন"
                    coroutineScope.launch {
                        // Shake effect
                        repeat(3) {
                            shakeOffset.animateTo(20f, spring(stiffness = Spring.StiffnessHigh))
                            shakeOffset.animateTo(-20f, spring(stiffness = Spring.StiffnessHigh))
                        }
                        shakeOffset.animateTo(0f)
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun onBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            isError = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A), // Dark Slate
                        Color(0xFF022C22), // Deep Emerald
                        Color(0xFF0F172A)
                    )
                )
            )
            .testTag("screen_app_lock"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Lock Badge & Branding
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF10B981), Color(0xFF0D9488))
                        )
                    )
                    .border(2.dp, Color(0xFF34D399).copy(alpha = 0.5f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "App Locked",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Shop Name & Subtitle
            val displayName = shopName?.takeIf { it.isNotBlank() && it != "মাওয়া ডিজিটাল খাতা" } ?: "দোকান খাতা ও ক্যাশ"
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, Color(0xFF10B981))
                ) {
                    Text(
                        text = "MAWA স্মার্ট সিকিউরিটি",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF6EE7B7),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "গোপনীয়তা রক্ষায় ৪ সংখ্যার পিন দিন",
                fontSize = 14.sp,
                color = Color(0xFFCBD5E1),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 4 Dots PIN Indicator with Shake animation
            Row(
                modifier = Modifier
                    .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isError -> FinancialNegative
                                    isFilled -> Color(0xFF10B981)
                                    else -> Color(0xFF334155)
                                }
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isError) FinancialNegative else if (isFilled) Color(0xFF34D399) else Color(0xFF64748B),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Error notice
            AnimatedVisibility(visible = isError, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = errorMessage,
                    color = Color(0xFFF87171),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Tactile Numeric Keypad
            val keys = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("CLEAR", "0", "DEL")
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for (row in keys) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (key in row) {
                            when (key) {
                                "CLEAR" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(bounded = true, color = Color.White)
                                            ) {
                                                enteredPin = ""
                                                isError = false
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "মুছুন",
                                            color = Color(0xFF94A3B8),
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                "DEL" -> {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(bounded = true, color = Color.White)
                                            ) { onBackspace() },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Backspace",
                                            tint = Color(0xFFE2E8F0),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                                else -> {
                                    val bengaliDigit = BengaliUtils.toBanglaDigits(key)
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B).copy(alpha = 0.8f))
                                            .border(1.dp, Color(0xFF334155), CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(bounded = true, color = Color(0xFF10B981))
                                            ) { onDigitPress(key) }
                                            .testTag("keypad_digit_$key"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = bengaliDigit,
                                                color = Color.White,
                                                fontSize = 22.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = key,
                                                color = Color(0xFF64748B),
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Forgot PIN Recovery
            TextButton(
                onClick = { showForgotDialog = true },
                modifier = Modifier.testTag("btn_forgot_pin")
            ) {
                Text(
                    text = "পিন ভুলে গেছেন? রিকভারি করুন",
                    color = Color(0xFF38BDF8),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Forgot PIN Dialog
    if (showForgotDialog) {
        var recoveryInput by remember { mutableStateOf("") }
        var newPinInput by remember { mutableStateOf("") }
        var confirmPinInput by remember { mutableStateOf("") }
        var recoveryError by remember { mutableStateOf("") }
        var isRecoveryVerified by remember { mutableStateOf(false) }

        val registeredPhone = AppLockManager.getRecoveryPhone(context)

        AlertDialog(
            onDismissRequest = { showForgotDialog = false },
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (!isRecoveryVerified) "পিন রিকভারি" else "নতুন পিন সেট করুন",
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (!isRecoveryVerified) {
                        Text(
                            text = if (registeredPhone.isNotBlank())
                                "দোকানদার হিসেবে ভেরিফাই করতে আপনার নিবন্ধিত মোবাইল নম্বর দিন:"
                            else
                                "দোকানের মালিকের মোবাইল নম্বর অথবা উত্তর দিয়ে পিন রিসেট করুন:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedTextField(
                            value = recoveryInput,
                            onValueChange = {
                                recoveryInput = it
                                recoveryError = ""
                            },
                            label = { Text("মোবাইল নম্বর") },
                            placeholder = { Text("যেমন: ০১৭১২-৩৪৫৬৭৮") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        Text(
                            text = "পরিচয় নিশ্চিত হয়েছে! নতুন ৪ সংখ্যার পিন কোড দিন:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = newPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                            label = { Text("নতুন ৪ সংখ্যার পিন") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = confirmPinInput,
                            onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) confirmPinInput = it },
                            label = { Text("পিন পুনরায় লিখুন") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (recoveryError.isNotBlank()) {
                        Text(
                            text = recoveryError,
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (!isRecoveryVerified) {
                            if (recoveryInput.isBlank()) {
                                recoveryError = "মোবাইল নম্বর লিখুন"
                                return@Button
                            }
                            val matches = AppLockManager.verifyRecovery(context, recoveryInput) ||
                                    (registeredPhone.isBlank() && recoveryInput.length >= 10)
                            if (matches) {
                                isRecoveryVerified = true
                                recoveryError = ""
                            } else {
                                recoveryError = "সঠিক নম্বর মেলেনি! নিবন্ধিত নম্বর দিন"
                            }
                        } else {
                            if (newPinInput.length != 4) {
                                recoveryError = "নতুন পিন অবশ্যই ৪ সংখ্যার হতে হবে"
                                return@Button
                            }
                            if (newPinInput != confirmPinInput) {
                                recoveryError = "দুটো পিন এক হয়নি! আবার লিখুন"
                                return@Button
                            }
                            AppLockManager.resetPin(context, newPinInput)
                            Toast.makeText(context, "নতুন পিন সফলভাবে সংরক্ষিত হয়েছে!", Toast.LENGTH_SHORT).show()
                            showForgotDialog = false
                            onUnlocked()
                        }
                    }
                ) {
                    Text(if (!isRecoveryVerified) "যাচাই করুন" else "পিন পরিবর্তন সম্পন্ন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }
}

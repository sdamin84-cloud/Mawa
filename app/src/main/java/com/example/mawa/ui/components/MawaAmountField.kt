package com.example.mawa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.VoiceParsedResult
import com.example.ui.theme.MawaPrimary
import androidx.compose.material.icons.filled.AutoAwesome

@Composable
fun MawaAmountInput(
    amount: String,
    onAmountChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "টাকার পরিমাণ",
    imeAction: ImeAction = ImeAction.Done,
    onDone: () -> Unit = {},
    onNext: () -> Unit = {},
    keyboardActions: KeyboardActions? = null,
    focusRequester: FocusRequester? = null,
    quickAmounts: List<Int> = listOf(50, 100, 500, 1000, 2000, 5000),
    testTag: String = "amount_input",
    onSmartInput: ((name: String, amount: String) -> Unit)? = null,
    onVoiceInput: ((VoiceParsedResult) -> Unit)? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = amount,
            onValueChange = { newVal ->
                if (onSmartInput != null) {
                    val parsed = com.example.mawa.util.SmartExpenseInputParser.parse(newVal)
                    if (parsed != null) {
                        onSmartInput(parsed.name, parsed.amountString)
                        return@OutlinedTextField
                    }
                    // Allow typing numbers, text, letters, spaces freely when smart input is enabled
                    onAmountChange(newVal)
                } else {
                    val converted = BengaliUtils.toEnglishDigits(newVal)
                    // Allow only digits and optional decimal point for purely numeric amount input
                    if (converted.isEmpty() || converted.matches(Regex("^\\d*\\.?\\d*$"))) {
                        onAmountChange(converted)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .then(if (focusRequester != null) Modifier.focusRequester(focusRequester) else Modifier)
                .onKeyEvent { event ->
                    if (event.type == KeyEventType.KeyUp && (event.key == Key.Enter || event.key == Key.NumPadEnter)) {
                        if (onSmartInput != null) {
                            val parsed = com.example.mawa.util.SmartExpenseInputParser.parse(amount)
                            if (parsed != null) {
                                onSmartInput(parsed.name, parsed.amountString)
                            }
                        }
                        if (imeAction == ImeAction.Done) onDone() else onNext()
                        true
                    } else {
                        false
                    }
                }
                .testTag(testTag),
            label = { Text(label) },
            placeholder = { Text(if (onSmartInput != null) "টাকা বা যেমন: মাছ ৩৫০" else "0") },
            leadingIcon = {
                Text(
                    text = "৳",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MawaPrimary,
                    modifier = Modifier.padding(start = 12.dp, end = 4.dp)
                )
            },
            trailingIcon = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    if (amount.isNotEmpty()) {
                        IconButton(
                            onClick = { onAmountChange("") },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "মুছুন",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (onVoiceInput != null) {
                        VoiceInputButton(
                            onVoiceResult = onVoiceInput,
                            isPillStyle = false,
                            modifier = Modifier.size(36.dp),
                            testTag = "amount_field_voice_mic_btn"
                        )
                    }
                }
            },
            textStyle = TextStyle(
                fontSize = if (amount.any { it.isLetter() }) 18.sp else 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            ),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (onSmartInput != null) KeyboardType.Text else KeyboardType.Number,
                imeAction = imeAction
            ),
            keyboardActions = keyboardActions ?: KeyboardActions(
                onDone = {
                    if (onSmartInput != null) {
                        val parsed = com.example.mawa.util.SmartExpenseInputParser.parse(amount)
                        if (parsed != null) {
                            onSmartInput(parsed.name, parsed.amountString)
                        }
                    }
                    onDone()
                },
                onNext = {
                    if (onSmartInput != null) {
                        val parsed = com.example.mawa.util.SmartExpenseInputParser.parse(amount)
                        if (parsed != null) {
                            onSmartInput(parsed.name, parsed.amountString)
                        }
                    }
                    onNext()
                }
            ),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MawaPrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface
            )
        )

        // Smart Detected Badge Chip
        if (onSmartInput != null) {
            val smartDetected = com.example.mawa.util.SmartExpenseInputParser.parse(amount)
            if (smartDetected != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MawaPrimary.copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onSmartInput(smartDetected.name, smartDetected.amountString)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = MawaPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "শনাক্ত হয়েছে: ${smartDetected.name} ➔ ৳${BengaliUtils.toBanglaDigits(smartDetected.amountString)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MawaPrimary
                            )
                        }
                        Text(
                            text = "সেট করুন ➔",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MawaPrimary
                        )
                    }
                }
            }
        }

        // Quick amount chip buttons
        if (quickAmounts.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickAmounts.take(4).forEach { quickVal ->
                    val isSelected = amount == quickVal.toString()
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                if (isSelected) MawaPrimary.copy(alpha = 0.15f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MawaPrimary else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                val currentVal = amount.toDoubleOrNull() ?: 0.0
                                onAmountChange((currentVal + quickVal).toInt().toString())
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+${BengaliUtils.toBanglaDigits(quickVal.toLong())}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isSelected) MawaPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MawaEmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(14.dp))
            Surface(
                onClick = onActionClick,
                shape = RoundedCornerShape(8.dp),
                color = MawaPrimary,
                contentColor = Color.White
            ) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }
        }
    }
}

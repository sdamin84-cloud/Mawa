package com.example.mawa.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.model.AppMode

@Composable
fun UserModePreferenceDialog(
    currentMode: AppMode,
    isFirstTime: Boolean = false,
    onDismiss: () -> Unit,
    onSelectMode: (AppMode) -> Unit
) {
    var selectedMode by remember { mutableStateOf(currentMode) }

    AlertDialog(
        onDismissRequest = {
            if (!isFirstTime) onDismiss()
        },
        title = {
            Column {
                Text(
                    text = "আপনি Mawa কীভাবে ব্যবহার করবেন?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "আপনার প্রয়োজন অনুযায়ী উপযুক্ত মোড বেছে নিন। পরবর্তীতে সেটিংস থেকে পরিবর্তন করা যাবে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ModeOptionCard(
                    mode = AppMode.BUSINESS_ONLY,
                    title = "ব্যবসার হিসাব",
                    subtitle = "দোকানের ক্যাশ, বাকি খাতা ও মালামাল কেনা-বেচা",
                    icon = Icons.Default.Store,
                    iconTint = MaterialTheme.colorScheme.primary,
                    isSelected = selectedMode == AppMode.BUSINESS_ONLY,
                    onClick = { selectedMode = AppMode.BUSINESS_ONLY },
                    testTag = "opt_business_only"
                )

                ModeOptionCard(
                    mode = AppMode.PERSONAL_ONLY,
                    title = "ব্যক্তিগত হিসাব",
                    subtitle = "দৈনন্দিন খরচ, মাসিক বাজেট, আয় ও সঞ্চয়ের ডায়েরি",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconTint = Color(0xFF00796B),
                    isSelected = selectedMode == AppMode.PERSONAL_ONLY,
                    onClick = { selectedMode = AppMode.PERSONAL_ONLY },
                    testTag = "opt_personal_only"
                )

                ModeOptionCard(
                    mode = AppMode.BOTH,
                    title = "দুটোই",
                    subtitle = "ব্যবসা ও ব্যক্তিগত হিসাব একই অ্যাপে সহজে পরিচালনা",
                    icon = Icons.Default.Dashboard,
                    iconTint = Color(0xFF6750A4),
                    isSelected = selectedMode == AppMode.BOTH,
                    onClick = { selectedMode = AppMode.BOTH },
                    testTag = "opt_both"
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSelectMode(selectedMode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("btn_confirm_mode")
            ) {
                Text(
                    text = if (isFirstTime) "শুরু করুন" else "সংরক্ষণ করুন",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            if (!isFirstTime) {
                TextButton(onClick = onDismiss) {
                    Text("বাতিল")
                }
            }
        }
    )
}

@Composable
private fun ModeOptionCard(
    mode: AppMode,
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val bgColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp
                )
            }

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "নির্বাচিত",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

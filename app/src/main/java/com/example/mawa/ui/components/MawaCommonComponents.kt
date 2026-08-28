package com.example.mawa.ui.components

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.util.BengaliUtils
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialNeutral
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary

@Composable
fun MawaSummaryRow(
    label: String,
    amount: Double,
    modifier: Modifier = Modifier,
    amountColor: Color = MaterialTheme.colorScheme.onSurface,
    showDivider: Boolean = true,
    isSubRow: Boolean = false,
    prefix: String = "",
    suffixText: String? = null,
    icon: ImageVector? = null,
    iconTint: Color? = null,
    onClick: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = if (isSubRow) 8.dp else 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                if (icon != null) {
                    val tint = iconTint ?: MaterialTheme.colorScheme.primary
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = tint.copy(alpha = if (isSubRow) 0.1f else 0.15f),
                        modifier = Modifier.size(if (isSubRow) 26.dp else 30.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = tint,
                                modifier = Modifier.size(if (isSubRow) 14.dp else 16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                } else if (isSubRow) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.outlineVariant)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }
                Text(
                    text = label,
                    style = if (isSubRow) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = if (isSubRow) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                    fontWeight = if (isSubRow) FontWeight.Normal else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$prefix${BengaliUtils.formatTaka(amount)}",
                    style = if (isSubRow) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = amountColor
                )
                if (suffixText != null) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = suffixText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun MawaQuickActionButton(
    label: String,
    icon: ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(containerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun TransactionItemRow(
    transaction: TransactionEntity,
    modifier: Modifier = Modifier,
    onDelete: (() -> Unit)? = null
) {
    val (icon, tint, bgColor, title, amountSign, amountColor) = when (transaction.type) {
        TransactionType.SALE_CASH -> {
            Tuple6(
                Icons.Default.ArrowUpward,
                FinancialPositive,
                FinancialPositiveContainer,
                "নগদ বিক্রি",
                "+",
                FinancialPositive
            )
        }
        TransactionType.SALE_BAKI -> {
            Tuple6(
                Icons.Default.Person,
                FinancialNegative,
                FinancialNegativeContainer,
                "বাকি বিক্রি: ${transaction.customerName ?: "কাস্টমার"}",
                "+",
                FinancialNegative
            )
        }
        TransactionType.BAKI_COLLECTION -> {
            Tuple6(
                Icons.Default.ArrowDownward,
                FinancialPositive,
                FinancialPositiveContainer,
                "বাকি আদায়: ${transaction.customerName ?: "কাস্টমার"}",
                "+",
                FinancialPositive
            )
        }
        TransactionType.PURCHASE_FORDI, TransactionType.PURCHASE_DIRECT -> {
            val name = transaction.productName ?: "মাল ক্রয়"
            val qtyStr = if (transaction.quantity > 0) " (${BengaliUtils.formatQuantity(transaction.quantity, transaction.unit)})" else ""
            Tuple6(
                Icons.Default.ShoppingCart,
                FinancialNeutral,
                MaterialTheme.colorScheme.surfaceVariant,
                "মাল কেনা: $name$qtyStr",
                "−",
                MaterialTheme.colorScheme.onSurface
            )
        }
        TransactionType.EXPENSE_SHOP -> {
            Tuple6(
                Icons.Default.Store,
                FinancialNegative,
                FinancialNegativeContainer,
                "দোকানের খরচ: ${transaction.note.ifBlank { "সাধারণ খরচ" }}",
                "−",
                FinancialNegative
            )
        }
        TransactionType.EXPENSE_HOME -> {
            Tuple6(
                Icons.Default.Home,
                FinancialWarning,
                FinancialWarningContainer,
                "বাড়ির জন্য: ${transaction.note.ifBlank { "বাড়ির খরচ" }}",
                "−",
                FinancialWarning
            )
        }
        TransactionType.CASH_ADJUSTMENT -> {
            Tuple6(
                Icons.Default.AttachMoney,
                MawaPrimary,
                MaterialTheme.colorScheme.primaryContainer,
                "ক্যাশ সমন্বয়: ${transaction.note}",
                if (transaction.amount >= 0) "+" else "−",
                MawaPrimary
            )
        }
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = BengaliUtils.formatTransactionTime(transaction.timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "$amountSign${BengaliUtils.formatTaka(transaction.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }

        if (onDelete != null) {
            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .size(32.dp)
                    .padding(start = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "মুছুন",
                    tint = MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

private data class Tuple6<A, B, C, D, E, F>(
    val a: A, val b: B, val c: C, val d: D, val e: E, val f: F
)

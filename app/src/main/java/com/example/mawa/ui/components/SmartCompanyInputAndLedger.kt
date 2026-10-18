package com.example.mawa.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Payment
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.mawa.data.local.entity.FordiItemEntity
import com.example.mawa.data.local.entity.ProductEntity
import com.example.mawa.data.local.entity.SupplierCompanyEntity
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.ui.theme.FinancialNegative
import com.example.ui.theme.FinancialNegativeContainer
import com.example.ui.theme.FinancialPositive
import com.example.ui.theme.FinancialPositiveContainer
import com.example.ui.theme.FinancialWarning
import com.example.ui.theme.FinancialWarningContainer
import com.example.ui.theme.MawaPrimary
import com.example.ui.theme.MawaPrimaryContainer
import com.example.mawa.util.BengaliUtils
import com.example.mawa.util.DataBackupRestoreManager
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Data class representing a parsed smart company/product query
 */
data class SmartParsedInput(
    val originalText: String,
    val detectedName: String,
    val detectedAmount: Double,
    val matchedSuggestion: CompanySuggestion? = null
)

/**
 * Dynamic suggestion generated strictly from existing database transactions & products
 */
data class CompanySuggestion(
    val name: String,
    val totalSpent: Double,
    val count: Int,
    val lastUsedTimestamp: Long,
    val lastAmount: Double
)

object SmartInputParser {

    /**
     * Converts Bengali digit string to standard English string for numeric parsing
     */
    fun convertToEnglishDigits(input: String): String {
        val bengaliDigits = "০১২৩৪৫৬৭৮৯"
        val sb = StringBuilder()
        for (ch in input) {
            val idx = bengaliDigits.indexOf(ch)
            if (idx != -1) {
                sb.append(idx)
            } else {
                sb.append(ch)
            }
        }
        return sb.toString()
    }

    /**
     * Parses free-form text like "আবুল খায়ের ৩৫০" or "অলি 340" or "প্রাণ দুধ ৫০" into Name and Amount
     */
    fun parse(input: String, historicalSuggestions: List<CompanySuggestion>): SmartParsedInput {
        val trimmed = input.trim()
        if (trimmed.isBlank()) {
            return SmartParsedInput(originalText = "", detectedName = "", detectedAmount = 0.0)
        }

        val englishConverted = convertToEnglishDigits(trimmed)
        // Find trailing or isolated numbers
        val numberRegex = Regex("""(?:\s|^)(\d+(?:\.\d+)?)(?:\s|$)""")
        val match = numberRegex.findAll(englishConverted).lastOrNull()

        var detectedAmount = 0.0
        var detectedName = trimmed

        if (match != null) {
            val numStr = match.groupValues[1]
            detectedAmount = numStr.toDoubleOrNull() ?: 0.0
            val range = match.range
            // Remove the number from the original text to get the clean name
            val namePart = StringBuilder(trimmed).delete(range.first.coerceAtLeast(0), (range.last + 1).coerceAtMost(trimmed.length)).toString().trim()
            detectedName = namePart
        }

        // Fuzzy/Prefix match against dynamic historical companies/products
        var bestMatch: CompanySuggestion? = null
        if (detectedName.isNotBlank()) {
            val normalizedQuery = detectedName.trim().lowercase(Locale.ROOT)
            bestMatch = historicalSuggestions.firstOrNull { suggestion ->
                val normSug = suggestion.name.trim().lowercase(Locale.ROOT)
                normSug.startsWith(normalizedQuery) || normSug.contains(normalizedQuery)
            }
        }

        return SmartParsedInput(
            originalText = trimmed,
            detectedName = detectedName,
            detectedAmount = detectedAmount,
            matchedSuggestion = bestMatch
        )
    }

    /**
     * Dynamically aggregates all company/product names from user's transactions, fordi items, and products
     * NO hardcoded data! Pure dynamic data science.
     */
    fun buildHistoricalSuggestions(
        transactions: List<TransactionEntity>,
        fordiItems: List<FordiItemEntity>,
        products: List<ProductEntity>
    ): List<CompanySuggestion> {
        val map = mutableMapOf<String, MutableList<TransactionEntity>>()

        // 1. Group purchase and expense transactions by description / product name
        for (tx in transactions) {
            if (tx.type == TransactionType.EXPENSE_SHOP ||
                tx.type == TransactionType.EXPENSE_HOME ||
                tx.type == TransactionType.PURCHASE_DIRECT ||
                tx.type == TransactionType.PURCHASE_FORDI
            ) {
                val name = tx.productName?.trim()?.ifBlank { null }
                    ?: tx.note.trim().ifBlank { null }
                    ?: tx.category.trim().ifBlank { null }

                if (!name.isNullOrBlank() && name.length >= 2) {
                    // Clean up generic prefixes if any
                    val cleanName = name.replace(Regex("""^(খরচ|দোকান|মাল|ক্রয়|বাকি|বিল)\s*:\s*"""), "").trim()
                    if (cleanName.isNotBlank()) {
                        map.getOrPut(cleanName) { mutableListOf() }.add(tx)
                    }
                }
            }
        }

        // 2. Add Fordi Items
        for (item in fordiItems) {
            val name = item.productName.trim()
            if (name.length >= 2 && !map.containsKey(name)) {
                map[name] = mutableListOf()
            }
        }

        // 3. Add Products
        for (prod in products) {
            val name = prod.name.trim()
            if (name.length >= 2 && !map.containsKey(name)) {
                map[name] = mutableListOf()
            }
        }

        // 4. Convert to rich suggestion models sorted by usage frequency and recency
        return map.map { (name, txList) ->
            val totalSpent = txList.sumOf { it.amount }
            val count = txList.size
            val lastTx = txList.maxByOrNull { it.timestamp }
            CompanySuggestion(
                name = name,
                totalSpent = totalSpent,
                count = count,
                lastUsedTimestamp = lastTx?.timestamp ?: 0L,
                lastAmount = lastTx?.amount ?: 0.0
            )
        }.sortedWith(
            compareByDescending<CompanySuggestion> { it.count }
                .thenByDescending { it.lastUsedTimestamp }
                .thenByDescending { it.totalSpent }
        )
    }
}

/**
 * Modern Smart Company & Product Quick Entry Bar on Home Screen
 */
@Composable
fun SmartCompanyQuickEntryBar(
    transactions: List<TransactionEntity>,
    fordiItems: List<FordiItemEntity>,
    products: List<ProductEntity>,
    supplierCompanies: List<SupplierCompanyEntity> = emptyList(),
    onSaveSupplierCompany: ((SupplierCompanyEntity) -> Unit)? = null,
    onAddShopExpense: (amount: Double, companyName: String) -> Unit,
    onAddDirectPurchase: (amount: Double, companyName: String) -> Unit,
    onAddToFordi: (companyName: String, rate: Double) -> Unit,
    onAddShopExpenseWithStatus: ((amount: Double, companyName: String, status: String) -> Unit)? = null,
    onAddDirectPurchaseWithStatus: ((amount: Double, companyName: String, status: String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var selectedCompanyForLedger by remember { mutableStateOf<String?>(null) }

    // Dynamic historical suggestions from Room DB
    val allSuggestions = remember(transactions, fordiItems, products) {
        SmartInputParser.buildHistoricalSuggestions(transactions, fordiItems, products)
    }

    val parsed = remember(inputText, allSuggestions) {
        SmartInputParser.parse(inputText, allSuggestions)
    }

    // Filter matching suggestions for autocomplete chips
    val matchingSuggestions = remember(inputText, allSuggestions) {
        if (inputText.isBlank()) {
            allSuggestions.take(8)
        } else {
            val query = parsed.detectedName.lowercase(Locale.ROOT).trim()
            if (query.isBlank()) {
                allSuggestions.take(8)
            } else {
                allSuggestions.filter {
                    it.name.lowercase(Locale.ROOT).contains(query)
                }.take(10)
            }
        }
    }

    val isDark = isSystemInDarkTheme()
    val brandPrimary = MaterialTheme.colorScheme.primary
    val barBg = MaterialTheme.colorScheme.surface
    val barBorder = if (isDark) MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("smart_company_quick_entry_card")
    ) {
        // Minimal & Professional Unified Search Capsule (Only 46dp high)
        Surface(
            shape = RoundedCornerShape(23.dp),
            color = barBg,
            border = BorderStroke(1.dp, barBorder),
            shadowElevation = 1.dp,
            modifier = Modifier
                .fillMaxWidth()
                .height(46.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek Search Icon
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "সার্চ",
                    tint = brandPrimary,
                    modifier = Modifier.size(19.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Minimal Clean Text Input (BasicTextField)
                BasicTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("smart_company_input_field"),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 13.sp
                    ),
                    cursorBrush = SolidColor(brandPrimary),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = if (parsed.detectedAmount > 0) ImeAction.Done else ImeAction.Search
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            val finalName = parsed.matchedSuggestion?.name ?: parsed.detectedName.ifBlank { "দোকানের খরচ" }
                            if (parsed.detectedAmount > 0) {
                                onAddShopExpense(parsed.detectedAmount, finalName)
                                inputText = ""
                            }
                        },
                        onSearch = {
                            val nameToSearch = parsed.matchedSuggestion?.name ?: parsed.detectedName
                            if (nameToSearch.isNotBlank()) {
                                selectedCompanyForLedger = nameToSearch
                            } else if (inputText.isNotBlank()) {
                                selectedCompanyForLedger = inputText.trim()
                            }
                        }
                    ),
                    decorationBox = { innerTextField ->
                        if (inputText.isEmpty()) {
                            Text(
                                text = "কোম্পানি, পণ্য বা দ্রুত খরচ খুঁজুন (যেমন: অলি ৩৪০)...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                fontSize = 12.5.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        innerTextField()
                    }
                )

                // Trailing Actions (Compact & Integrated Inside Search Bar)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (inputText.isNotBlank()) {
                        IconButton(
                            onClick = { inputText = "" },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "পরিষ্কার করুন",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Compact Circular Voice Input Button (32dp)
                    VoiceInputButton(
                        onVoiceResult = { voiceResult ->
                            val amt = voiceResult.amount ?: 0.0
                            inputText = voiceResult.rawText.ifBlank {
                                if (amt > 0) "${voiceResult.note} ${BengaliUtils.toBanglaDigits(amt.toLong())}"
                                else voiceResult.note
                            }
                        },
                        modifier = Modifier.size(32.dp),
                        testTag = "smart_company_voice_btn"
                    )

                    // Compact Circular Search Button (32dp)
                    Surface(
                        onClick = {
                            val nameToSearch = parsed.matchedSuggestion?.name ?: parsed.detectedName
                            if (nameToSearch.isNotBlank()) {
                                selectedCompanyForLedger = nameToSearch
                            } else if (inputText.isNotBlank()) {
                                selectedCompanyForLedger = inputText.trim()
                            }
                        },
                        shape = CircleShape,
                        color = brandPrimary,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("smart_company_search_btn")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "সার্চ করুন",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }

        // Expanded Section Only Visible When User Types
        AnimatedVisibility(
            visible = inputText.isNotBlank(),
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(modifier = Modifier.padding(top = 8.dp)) {
                val resolvedName = parsed.matchedSuggestion?.name ?: parsed.detectedName

                // Compact Smart Detection Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (parsed.detectedAmount > 0) FinancialPositiveContainer.copy(alpha = 0.55f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, if (parsed.detectedAmount > 0) FinancialPositive.copy(alpha = 0.35f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = resolvedName.ifBlank { "অনির্দিষ্ট" },
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (parsed.detectedAmount > 0) {
                                    Text(
                                        text = " · ৳${BengaliUtils.formatTaka(parsed.detectedAmount)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FinancialPositive
                                    )
                                }
                            }

                            if (resolvedName.isNotBlank()) {
                                OutlinedButton(
                                    onClick = { selectedCompanyForLedger = resolvedName },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Icon(Icons.Default.History, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("লেজার", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Instant Action Buttons
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (parsed.detectedAmount > 0) {
                                Button(
                                    onClick = {
                                        val finalName = resolvedName.ifBlank { "দোকানের খরচ" }
                                        onAddShopExpense(parsed.detectedAmount, finalName)
                                        inputText = ""
                                    },
                                    modifier = Modifier
                                        .weight(1.2f)
                                        .height(32.dp)
                                        .testTag("smart_add_expense_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "খরচ (+${BengaliUtils.formatTaka(parsed.detectedAmount)})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = {
                                        val finalName = resolvedName.ifBlank { "মাল ক্রয়" }
                                        onAddDirectPurchase(parsed.detectedAmount, finalName)
                                        inputText = ""
                                    },
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .height(32.dp)
                                        .testTag("smart_add_purchase_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "পণ্য ক্রয়",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            if (resolvedName.isNotBlank()) {
                                OutlinedButton(
                                    onClick = {
                                        onAddToFordi(resolvedName, parsed.detectedAmount)
                                        inputText = ""
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(32.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("ফর্দে যোগ", fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // Compact Suggestion Pills Underneath Active Search
                if (matchingSuggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        matchingSuggestions.forEach { suggestion ->
                            val isProduct = products.any { it.name.trim().equals(suggestion.name.trim(), ignoreCase = true) }
                            val chipBg = if (isDark) {
                                if (isProduct) Color(0xFF064E3B).copy(alpha = 0.45f) else Color(0xFF312E81).copy(alpha = 0.45f)
                            } else {
                                if (isProduct) Color(0xFFECFDF5) else Color(0xFFEEF2FF)
                            }
                            val chipBorder = if (isDark) {
                                if (isProduct) Color(0xFF059669).copy(alpha = 0.6f) else Color(0xFF6366F1).copy(alpha = 0.6f)
                            } else {
                                if (isProduct) Color(0xFFA7F3D0) else Color(0xFFC7D2FE)
                            }
                            val chipText = if (isDark) {
                                if (isProduct) Color(0xFF6EE7B7) else Color(0xFFA5B4FC)
                            } else {
                                if (isProduct) Color(0xFF047857) else Color(0xFF4338CA)
                            }

                            Surface(
                                onClick = {
                                    if (parsed.detectedAmount > 0) {
                                        inputText = "${suggestion.name} ${BengaliUtils.toBanglaDigits(parsed.detectedAmount.toLong())}"
                                    } else {
                                        inputText = suggestion.name
                                    }
                                },
                                shape = RoundedCornerShape(13.dp),
                                color = chipBg,
                                border = BorderStroke(1.dp, chipBorder),
                                modifier = Modifier.height(26.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Text(
                                        text = if (isProduct) "📦 ${suggestion.name}" else "🏢 ${suggestion.name}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = chipText,
                                        fontSize = 11.sp
                                    )
                                    if (suggestion.lastAmount > 0) {
                                        Text(
                                            text = "৳${BengaliUtils.toBanglaDigits(suggestion.lastAmount.toLong())}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontSize = 9.5.sp,
                                            color = chipText,
                                            fontWeight = FontWeight.Bold
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

    // Dedicated Company & Product Profile Dialog
    if (selectedCompanyForLedger != null) {
        CompanyProductLedgerDialog(
            companyName = selectedCompanyForLedger!!,
            allTransactions = transactions,
            supplierCompanies = supplierCompanies,
            onSaveSupplierCompany = onSaveSupplierCompany,
            onDismiss = { selectedCompanyForLedger = null },
            onAddExpense = { amount, note ->
                onAddShopExpense(amount, note)
            },
            onAddPurchase = { amount, note ->
                onAddDirectPurchase(amount, note)
            },
            onAddToFordi = { name, rate ->
                onAddToFordi(name, rate)
            },
            onAddExpenseWithStatus = { amount, note, status ->
                onAddShopExpenseWithStatus?.invoke(amount, note, status) ?: onAddShopExpense(amount, note)
            },
            onAddPurchaseWithStatus = { amount, note, status ->
                onAddDirectPurchaseWithStatus?.invoke(amount, note, status) ?: onAddDirectPurchase(amount, note)
            }
        )
    }
}

/**
 * Dedicated Company & Product Profile & Transaction Ledger Dialog
 * Displays:
 * 1. Total purchases / expenses for this company
 * 2. Day of week, exact date, time, and amount of every transaction
 * 3. Separate Supplier Due / Baki Tracking
 * 4. 1-Click quick actions & PDF/Share summary
 */
@Composable
fun CompanyProductLedgerDialog(
    companyName: String,
    allTransactions: List<TransactionEntity>,
    supplierCompanies: List<SupplierCompanyEntity> = emptyList(),
    onSaveSupplierCompany: ((SupplierCompanyEntity) -> Unit)? = null,
    onDismiss: () -> Unit,
    onAddExpense: (amount: Double, note: String) -> Unit,
    onAddPurchase: (amount: Double, note: String) -> Unit,
    onAddToFordi: (name: String, rate: Double) -> Unit,
    onAddExpenseWithStatus: ((amount: Double, note: String, status: String) -> Unit)? = null,
    onAddPurchaseWithStatus: ((amount: Double, note: String, status: String) -> Unit)? = null
) {
    val context = LocalContext.current
    var newAmountInput by remember { mutableStateOf("") }
    var newNoteInput by remember { mutableStateOf("") }
    var showAddEntryBox by remember { mutableStateOf(false) }
    var paymentStatusChoice by remember { mutableStateOf("PAID") }
    var showEditSrDialog by remember { mutableStateOf(false) }

    val registeredCompany = remember(companyName, supplierCompanies) {
        val q = companyName.trim().lowercase(Locale.ROOT)
        supplierCompanies.firstOrNull { it.companyName.trim().lowercase(Locale.ROOT) == q }
            ?: supplierCompanies.firstOrNull {
                val sc = it.companyName.trim().lowercase(Locale.ROOT)
                sc.contains(q) || q.contains(sc)
            }
    }

    // Filter transactions matching this company/product
    val companyTransactions = remember(companyName, allTransactions) {
        val query = companyName.trim().lowercase(Locale.ROOT)
        allTransactions.filter { tx ->
            val pName = tx.productName?.lowercase(Locale.ROOT) ?: ""
            val note = tx.note.lowercase(Locale.ROOT)
            val cat = tx.category.lowercase(Locale.ROOT)
            pName.contains(query) || note.contains(query) || cat.contains(query)
        }.sortedByDescending { it.timestamp }
    }

    // Analytics calculation
    val totalSpent = remember(companyTransactions) {
        companyTransactions.sumOf { it.amount }
    }

    val thisMonthSpent = remember(companyTransactions) {
        val cal = Calendar.getInstance()
        val curMonth = cal.get(Calendar.MONTH)
        val curYear = cal.get(Calendar.YEAR)
        companyTransactions.filter { tx ->
            cal.timeInMillis = tx.timestamp
            cal.get(Calendar.MONTH) == curMonth && cal.get(Calendar.YEAR) == curYear
        }.sumOf { it.amount }
    }

    val avgSpent = remember(companyTransactions, totalSpent) {
        if (companyTransactions.isNotEmpty()) totalSpent / companyTransactions.size else 0.0
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .testTag("company_ledger_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Dialog Header
                Surface(
                    color = MawaPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = companyName,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "কোম্পানি ও পণ্য বিস্তারিত লেজার প্রোফাইল",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.85f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "বন্ধ করুন", tint = Color.White)
                        }
                    }
                }

                // Company SR Contact & Direct Call Profile Card
                if (onSaveSupplierCompany != null) {
                    Box(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
                        if (registeredCompany != null) {
                            CompanySrProfileCard(
                                company = registeredCompany,
                                onEditCompany = { showEditSrDialog = true }
                            )
                        } else {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { showEditSrDialog = true }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Business, contentDescription = null, tint = MawaPrimary, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "+ কোম্পানির সেলসম্যান/SR-এর ফোন নম্বর সেভ করুন (কল অপশন সহ)",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Icon(Icons.Default.Add, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Summary Stats Cards (মোট খরচ, চলতি মাস, ভাউচার সংখ্যা)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Spent
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = FinancialNegativeContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("সর্বমোট মাল নেওয়া", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = BengaliUtils.formatTaka(totalSpent),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = FinancialNegative
                            )
                        }
                    }

                    // This Month Spent
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MawaPrimaryContainer.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("চলতি মাসে নেওয়া", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = BengaliUtils.formatTaka(thisMonthSpent),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MawaPrimary
                            )
                        }
                    }

                    // Total Count & Avg
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("মোট চালান/বার", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = "${BengaliUtils.toBanglaDigits(companyTransactions.size.toLong())} টি",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Quick Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showAddEntryBox = !showAddEntryBox },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (showAddEntryBox) FinancialNegative else MawaPrimary)
                    ) {
                        Icon(
                            imageVector = if (showAddEntryBox) Icons.Default.Close else Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (showAddEntryBox) "ফর্ম বন্ধ করুন" else "নতুন এন্ট্রি যোগ করুন", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareSummary = buildString {
                                appendLine("🏢 $companyName - লেজার বিবরণী")
                                appendLine("মোট খরচ/ক্রয়: ৳${BengaliUtils.formatTaka(totalSpent)}")
                                appendLine("চলতি মাসে: ৳${BengaliUtils.formatTaka(thisMonthSpent)}")
                                appendLine("মোট চালান: ${companyTransactions.size} টি")
                                appendLine("--------------------------------")
                                companyTransactions.take(15).forEach { tx ->
                                    val dateStr = SimpleDateFormat("dd/MM/yyyy (EEE)", Locale("bn", "BD")).format(Date(tx.timestamp))
                                    appendLine("$dateStr: ৳${tx.amount.toInt()} (${tx.type.banglaLabel} ${tx.note})")
                                }
                            }
                            DataBackupRestoreManager.shareText(context, shareSummary, "$companyName লেজার বিবরণী")
                        },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "শেয়ার", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("শেয়ার", fontSize = 12.sp)
                    }
                }

                // Expandable Quick Entry Form
                AnimatedVisibility(visible = showAddEntryBox) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, MawaPrimary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "$companyName-এ নতুন লেনদেন / খরচ লিখুন:",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = newAmountInput,
                                    onValueChange = { newAmountInput = it },
                                    label = { Text("টাকার পরিমাণ") },
                                    placeholder = { Text("উদাঃ ৩৫০") },
                                    modifier = Modifier.weight(1f),
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    shape = RoundedCornerShape(10.dp)
                                )

                                OutlinedTextField(
                                    value = newNoteInput,
                                    onValueChange = { newNoteInput = it },
                                    label = { Text("বিবরণ / পণ্যের নাম") },
                                    placeholder = { Text("উদাঃ বিস্কুট বা তেল") },
                                    modifier = Modifier.weight(1.2f),
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Payment Status Selector: নগদ vs বিকালে বাকি
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (paymentStatusChoice == "PAID") FinancialPositiveContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { paymentStatusChoice = "PAID" }
                                ) {
                                    Text(
                                        text = "✅ নগদ পরিশোধ",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (paymentStatusChoice == "PAID") FinancialPositive else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (paymentStatusChoice == "DUE_PAYMENT") FinancialWarningContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { paymentStatusChoice = "DUE_PAYMENT" }
                                ) {
                                    Text(
                                        text = "🕒 বিকালে বাকি দেব",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (paymentStatusChoice == "DUE_PAYMENT") FinancialWarning else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(vertical = 6.dp),
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val eng = SmartInputParser.convertToEnglishDigits(newAmountInput)
                                        val amt = eng.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            val note = if (newNoteInput.isNotBlank()) "$companyName ($newNoteInput)" else companyName
                                            if (onAddExpenseWithStatus != null) {
                                                onAddExpenseWithStatus(amt, note, paymentStatusChoice)
                                            } else {
                                                onAddExpense(amt, note)
                                            }
                                            newAmountInput = ""
                                            newNoteInput = ""
                                            showAddEntryBox = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = FinancialNegative),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("খরচে যোগ (+)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                Button(
                                    onClick = {
                                        val eng = SmartInputParser.convertToEnglishDigits(newAmountInput)
                                        val amt = eng.toDoubleOrNull() ?: 0.0
                                        if (amt > 0) {
                                            val note = if (newNoteInput.isNotBlank()) "$companyName ($newNoteInput)" else companyName
                                            if (onAddPurchaseWithStatus != null) {
                                                onAddPurchaseWithStatus(amt, note, paymentStatusChoice)
                                            } else {
                                                onAddPurchase(amt, note)
                                            }
                                            newAmountInput = ""
                                            newNoteInput = ""
                                            showAddEntryBox = false
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = MawaPrimary),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("পণ্য ক্রয়ে যোগ", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = {
                                        val eng = SmartInputParser.convertToEnglishDigits(newAmountInput)
                                        val amt = eng.toDoubleOrNull() ?: 0.0
                                        val name = if (newNoteInput.isNotBlank()) "$companyName ($newNoteInput)" else companyName
                                        onAddToFordi(name, amt)
                                        newAmountInput = ""
                                        newNoteInput = ""
                                        showAddEntryBox = false
                                    },
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text("ফর্দে", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Timeline Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "সকল লেনদেনের তালিকা (তারিখ, বার ও সময়সহ)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Transaction Items List
                if (companyTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "এই কোম্পানির কোনো পূর্বের লেনদেন রেকর্ড পাওয়া যায়নি",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "উপরের 'নতুন এন্ট্রি যোগ করুন' বাটনে চাপ দিয়ে খরচ বা মাল ক্রয় যুক্ত করুন",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(horizontal = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(companyTransactions, key = { it.id }) { tx ->
                            val cal = Calendar.getInstance().apply { timeInMillis = tx.timestamp }
                            val dateStr = SimpleDateFormat("dd MMMM yyyy", Locale("bn", "BD")).format(Date(tx.timestamp))
                            val weekdayStr = SimpleDateFormat("EEEE", Locale("bn", "BD")).format(Date(tx.timestamp))
                            val timeStr = SimpleDateFormat("hh:mm a", Locale("bn", "BD")).format(Date(tx.timestamp))

                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                                border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.weight(1f),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    when (tx.type) {
                                                        TransactionType.EXPENSE_SHOP, TransactionType.EXPENSE_HOME -> FinancialNegativeContainer
                                                        TransactionType.PURCHASE_DIRECT, TransactionType.PURCHASE_FORDI -> MawaPrimaryContainer
                                                        else -> MaterialTheme.colorScheme.surfaceVariant
                                                    }
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = when (tx.type) {
                                                    TransactionType.EXPENSE_SHOP, TransactionType.EXPENSE_HOME -> Icons.Default.Receipt
                                                    TransactionType.PURCHASE_DIRECT, TransactionType.PURCHASE_FORDI -> Icons.Default.ShoppingCart
                                                    else -> Icons.Default.Payment
                                                },
                                                contentDescription = null,
                                                tint = when (tx.type) {
                                                    TransactionType.EXPENSE_SHOP, TransactionType.EXPENSE_HOME -> FinancialNegative
                                                    TransactionType.PURCHASE_DIRECT, TransactionType.PURCHASE_FORDI -> MawaPrimary
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(10.dp))

                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = dateStr,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = " ($weekdayStr)",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MawaPrimary,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }

                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = "⏰ $timeStr",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                Text(
                                                    text = "· ${tx.type.banglaLabel}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                                if (tx.note.isNotBlank() && tx.note != companyName) {
                                                    Text(
                                                        text = "· ${tx.note}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = BengaliUtils.formatTaka(tx.amount),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = FinancialNegative
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showEditSrDialog && onSaveSupplierCompany != null) {
            EditCompanySrDialog(
                initialCompany = registeredCompany,
                defaultName = companyName,
                onDismiss = { showEditSrDialog = false },
                onSave = { updated ->
                    onSaveSupplierCompany(updated)
                    showEditSrDialog = false
                }
            )
        }
    }
}

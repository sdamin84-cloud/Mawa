package com.example.mawa.data.model

import com.example.mawa.data.local.entity.PersonalTransactionEntity

data class CategorySpending(
    val category: String,
    val totalAmount: Double,
    val percentage: Float,
    val transactionCount: Int
)

data class PersonalSummary(
    val thisMonthExpense: Double = 0.0,
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val netBalance: Double = 0.0,
    val categoryBreakdown: List<CategorySpending> = emptyList(),
    val periodTransactions: List<PersonalTransactionEntity> = emptyList()
)

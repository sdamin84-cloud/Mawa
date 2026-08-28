package com.example.mawa

import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.AccountingSummary
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.util.BengaliUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class MawaAccountingTest {

    @Test
    fun testBengaliDigitConversion() {
        assertEquals("০", BengaliUtils.toBanglaDigits("0"))
        assertEquals("১২৩৪৫৬৭৮৯০", BengaliUtils.toBanglaDigits("1234567890"))
        assertEquals("৳১,৫০০", BengaliUtils.formatTaka(1500.0))
        assertEquals("৳০", BengaliUtils.formatTaka(0.0))
    }

    @Test
    fun testTotalSalesFormula() {
        // Total Sales = Cash Sales + Baki Sales
        val cashSales = 5000.0
        val bakiSales = 2000.0
        val bakiCollection = 1500.0 // Baki collection is NOT a sale!

        val totalSales = cashSales + bakiSales
        assertEquals(7000.0, totalSales, 0.001)
    }

    @Test
    fun testCashInHandFormula() {
        // Cash in Hand = Opening + Cash Sales + Baki Collection - Purchases - Shop Expenses - Home Withdrawals
        val opening = 10000.0
        val cashSales = 8000.0
        val bakiCollection = 2000.0
        val purchases = 4000.0
        val shopExpenses = 1000.0
        val homeWithdrawal = 3000.0

        val cashInHand = opening + cashSales + bakiCollection - purchases - shopExpenses - homeWithdrawal
        // 10000 + 8000 + 2000 - 4000 - 1000 - 3000 = 12000
        assertEquals(12000.0, cashInHand, 0.001)
    }

    @Test
    fun testEstimatedBusinessProfitAndRemaining() {
        // Business Profit = Total Sales - Purchases - Shop Expenses
        val totalSales = 15000.0
        val purchases = 9000.0
        val shopExpenses = 1500.0
        val homeWithdrawal = 2000.0

        val businessProfit = totalSales - purchases - shopExpenses // 15000 - 9000 - 1500 = 4500
        val profitRemaining = businessProfit - homeWithdrawal // 4500 - 2000 = 2500

        assertEquals(4500.0, businessProfit, 0.001)
        assertEquals(2500.0, profitRemaining, 0.001)
    }

    @Test
    fun testCustomerBakiBalanceCalculation() {
        val openingBaki = 500.0
        val bakiGiven = 1200.0
        val bakiPaid = 800.0

        val currentBalance = openingBaki + bakiGiven - bakiPaid
        assertEquals(900.0, currentBalance, 0.001)
    }

    @Test
    fun testProductMarginCalculation() {
        val purchaseRate = 120.0
        val sellingPrice = 145.0
        val margin = sellingPrice - purchaseRate
        assertEquals(25.0, margin, 0.001)
    }
}

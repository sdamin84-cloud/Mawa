package com.example.mawa

import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.AccountingSummary
import com.example.mawa.data.model.CustomerWithBalance
import com.example.mawa.util.BengaliUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
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

    @Test
    fun testKeyValueJsonBackupParsing() {
        val sampleJson = """
        {
          "key_baki_records": [
            {
              "amount": 50.0,
              "customerName": "টিপু",
              "date": "20/08/2026",
              "deletedAt": 0,
              "details": "",
              "dueDate": "",
              "id": "4e44d24c-3193-4064-aed9-d656e4ad3d06",
              "phone": "01700000000",
              "transactions": [
                {
                  "amount": 50.0,
                  "balanceAfter": 50.0,
                  "date": "20/08/2026",
                  "deletedAt": 0,
                  "id": "28000df2-801e-40d8-8c20-f7f43807b064",
                  "note": "নতুন বাকি শুরু",
                  "time": "06:28 PM",
                  "type": "BAKI",
                  "updatedAt": 1787228925047
                }
              ],
              "updatedAt": 1787228925047
            }
          ],
          "key_expenses_06-08-2026": [
            {
              "amount": 6787.0,
              "date": "06-08-2026",
              "deletedAt": 0,
              "expenseType": "SHOP",
              "id": "050c3abd-fbd6-412b-82b8-dd4f5b723216",
              "name": "মাল কেনা",
              "time": "09:17 PM",
              "type": "PURCHASE",
              "updatedAt": 1787671036534
            }
          ],
          "key_fordi_records": [
            {
              "colorHex": "#F0FDFA",
              "date": "21/08/2026",
              "deletedAt": 0,
              "id": "e97b466b-7a4d-41fa-bccf-de943594190a",
              "items": [
                {
                  "actualPurchaseRate": 185.0,
                  "actualQuantity": 0.0,
                  "actualTotal": 0.0,
                  "id": "ddcc6593-c0f4-4465-b8eb-5ce42d2ec20e",
                  "isChecked": false,
                  "name": "সয়াবিন তেল",
                  "plannedQuantity": 12.0,
                  "plannedTotal": 2220.0,
                  "postedToAccounting": false,
                  "potentialProfit": 180.0,
                  "price": 2220.0,
                  "productName": "সয়াবিন তেল",
                  "purchaseRate": 185.0,
                  "sellingRate": 200.0,
                  "status": "NOT_BOUGHT",
                  "unit": "liter"
                }
              ],
              "postedAmount": 0.0,
              "postedToAccounting": false,
              "status": "DRAFT",
              "title": "২১ আগস্ট বাজার ফর্দ",
              "updatedAt": 1787315775868
            }
          ],
          "key_product_memory": [
            {
              "averagePurchasePrice": 45.0,
              "category": "বিস্কুট",
              "createdAt": 1787228506180,
              "deletedAt": 0,
              "id": "1572c2f7-0693-4b84-9d93-1ebb544ec11d",
              "lastPurchaseDate": "20-08-2026",
              "lastPurchasePrice": 45.0,
              "name": "টোস্ট বিস্কুট",
              "purchaseCount": 1,
              "sellingPrice": 55.0,
              "unit": "packet",
              "updatedAt": 1787228506180
            }
          ],
          "key_sabek_cash_29-08-2026": 4180.0
        }
        """.trimIndent()

        val parsed = com.example.mawa.util.DataBackupRestoreManager.parseFromJsonString(sampleJson)
        assertEquals(1, parsed.customers.size)
        assertEquals("টিপু", parsed.customers[0].name)
        assertEquals(1, parsed.fordiItems.size)
        assertEquals("সয়াবিন তেল", parsed.fordiItems[0].productName)
        assertEquals(1, parsed.products.size)
        assertEquals("টোস্ট বিস্কুট", parsed.products[0].name)
        assertEquals(2, parsed.transactions.size) // 1 baki transaction + 1 purchase transaction
        assertEquals(4180.0, parsed.shopSettings?.openingBalance ?: 0.0, 0.001)
    }
}

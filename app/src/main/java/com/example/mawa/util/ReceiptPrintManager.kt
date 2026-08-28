package com.example.mawa.util

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.example.mawa.data.local.entity.TransactionEntity
import com.example.mawa.data.local.entity.TransactionType
import com.example.mawa.data.model.CustomerWithBalance
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptPrintManager {

    /**
     * Prints an HTML document using Android's native PrintManager and a hidden WebView.
     */
    fun printHtml(context: Context, htmlContent: String, jobName: String) {
        val activity = getActivity(context)
        if (activity == null) {
            Toast.makeText(context, "প্রিন্ট অপশন শুরু করা সম্ভব হয়নি", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            val webView = WebView(context)
            webView.webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                    return false
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
                    if (printManager != null) {
                        val printAdapter = webView.createPrintDocumentAdapter(jobName)
                        val printAttributes = PrintAttributes.Builder()
                            .setMediaSize(PrintAttributes.MediaSize.ISO_A4)
                            .setResolution(PrintAttributes.Resolution("mawa_print", "Mawa Print", 300, 300))
                            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
                            .build()

                        printManager.print(jobName, printAdapter, printAttributes)
                    } else {
                        Toast.makeText(context, "প্রিন্টার সার্ভিস পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        } catch (e: Exception) {
            Toast.makeText(context, "প্রিন্ট করতে সমস্যা হয়েছে: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun printHtmlDocument(context: Context, htmlContent: String, jobName: String) {
        printHtml(context, htmlContent, jobName)
    }

    private fun getActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) return ctx
            ctx = ctx.baseContext
        }
        return null
    }

    /**
     * Generates a beautifully styled Bengali Digital Memo / Invoice HTML.
     */
    fun generateInvoiceHtml(
        shopName: String,
        shopPhone: String,
        shopAddress: String,
        memoNo: String,
        dateFormatted: String,
        customerName: String?,
        customerPhone: String?,
        items: List<InvoiceItem>,
        subtotal: Double,
        discount: Double = 0.0,
        paidAmount: Double,
        previousDue: Double = 0.0,
        currentDue: Double = 0.0,
        note: String = ""
    ): String {
        val total = subtotal - discount
        val totalPayable = total + previousDue
        val remainingDue = currentDue

        val itemsHtml = StringBuilder()
        items.forEachIndexed { index, item ->
            val num = BengaliUtils.toBanglaDigits((index + 1).toLong())
            val qty = if (item.quantity > 0) "${BengaliUtils.toBanglaDigits(item.quantity)} ${item.unit}" else "-"
            val rate = if (item.rate > 0) BengaliUtils.formatTaka(item.rate) else "-"
            val amount = BengaliUtils.formatTaka(item.amount)

            itemsHtml.append("""
                <tr>
                    <td style="text-align: center; padding: 6px 4px; border-bottom: 1px dashed #ccc;">$num</td>
                    <td style="padding: 6px 4px; border-bottom: 1px dashed #ccc;">${item.name}</td>
                    <td style="text-align: center; padding: 6px 4px; border-bottom: 1px dashed #ccc;">$qty</td>
                    <td style="text-align: right; padding: 6px 4px; border-bottom: 1px dashed #ccc;">$rate</td>
                    <td style="text-align: right; padding: 6px 4px; border-bottom: 1px dashed #ccc; font-weight: bold;">$amount</td>
                </tr>
            """.trimIndent())
        }

        val custHtml = if (!customerName.isNullOrBlank()) {
            """
            <div style="margin-bottom: 12px; padding: 8px; background: #f9f9f9; border-radius: 6px; border: 1px solid #e0e0e0;">
                <div style="font-size: 13px; font-weight: bold; color: #1e293b;">গ্রাহক / ক্রেতার তথ্য:</div>
                <div style="font-size: 12px; color: #334155; margin-top: 2px;">নাম: <b>$customerName</b></div>
                ${if (!customerPhone.isNullOrBlank()) "<div style=\"font-size: 12px; color: #334155;\">মোবাইল: $customerPhone</div>" else ""}
            </div>
            """.trimIndent()
        } else ""

        val previousDueHtml = if (previousDue > 0) {
            """
            <tr>
                <td colspan="4" style="text-align: right; padding: 4px; color: #64748b;">পূর্বের বকেয়া:</td>
                <td style="text-align: right; padding: 4px; font-weight: bold; color: #dc2626;">${BengaliUtils.formatTaka(previousDue)}</td>
            </tr>
            """.trimIndent()
        } else ""

        val currentDueHtml = if (remainingDue > 0) {
            """
            <tr style="background: #fef2f2;">
                <td colspan="4" style="text-align: right; padding: 6px; font-weight: bold; color: #dc2626;">অবশিষ্ট বর্তমান বকেয়া:</td>
                <td style="text-align: right; padding: 6px; font-weight: bold; color: #dc2626; font-size: 14px;">${BengaliUtils.formatTaka(remainingDue)}</td>
            </tr>
            """.trimIndent()
        } else ""

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>$shopName - মেমো $memoNo</title>
            <style>
                @page { size: auto; margin: 15mm 10mm; }
                body {
                    font-family: 'SolaimanLipi', 'Kalpurush', 'Hind Siliguri', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #ffffff;
                    color: #1e293b;
                    margin: 0;
                    padding: 10px;
                    max-width: 480px;
                    margin-left: auto;
                    margin-right: auto;
                }
                .header {
                    text-align: center;
                    border-bottom: 2px solid #0f172a;
                    padding-bottom: 8px;
                    margin-bottom: 12px;
                }
                .shop-title {
                    font-size: 22px;
                    font-weight: bold;
                    color: #0f172a;
                    margin: 0;
                }
                .shop-sub {
                    font-size: 12px;
                    color: #475569;
                    margin: 2px 0;
                }
                .meta-row {
                    display: flex;
                    justify-content: space-between;
                    font-size: 12px;
                    color: #334155;
                    margin-bottom: 10px;
                }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    font-size: 12px;
                    margin-bottom: 12px;
                }
                th {
                    background: #f1f5f9;
                    color: #0f172a;
                    padding: 6px 4px;
                    border-top: 1px solid #cbd5e1;
                    border-bottom: 1px solid #cbd5e1;
                    font-weight: bold;
                }
                .footer {
                    margin-top: 20px;
                    text-align: center;
                    font-size: 11px;
                    color: #64748b;
                    border-top: 1px dashed #cbd5e1;
                    padding-top: 10px;
                }
                .badge {
                    display: inline-block;
                    padding: 3px 8px;
                    background: #e2e8f0;
                    border-radius: 4px;
                    font-size: 11px;
                    font-weight: bold;
                    margin-top: 4px;
                }
            </style>
        </head>
        <body>
            <div class="header">
                <h1 class="shop-title">$shopName</h1>
                ${if (shopAddress.isNotBlank()) "<div class=\"shop-sub\">$shopAddress</div>" else ""}
                ${if (shopPhone.isNotBlank()) "<div class=\"shop-sub\">মোবাইল: $shopPhone</div>" else ""}
                <div class="badge">ক্যাশ ও ডিজিটাল মেমো রসিদ</div>
            </div>

            <div class="meta-row">
                <div>মেমো নং: <b>$memoNo</b></div>
                <div>তারিখ: <b>$dateFormatted</b></div>
            </div>

            $custHtml

            <table>
                <thead>
                    <tr>
                        <th style="width: 25px; text-align: center;">নং</th>
                        <th style="text-align: left;">বিবরণ / পণ্য</th>
                        <th style="width: 50px; text-align: center;">পরিমাণ</th>
                        <th style="width: 55px; text-align: right;">দর</th>
                        <th style="width: 65px; text-align: right;">মোট</th>
                    </tr>
                </thead>
                <tbody>
                    $itemsHtml
                </tbody>
                <tfoot>
                    <tr>
                        <td colspan="4" style="text-align: right; padding: 6px 4px; font-weight: bold;">মোট মূল্য:</td>
                        <td style="text-align: right; padding: 6px 4px; font-weight: bold;">${BengaliUtils.formatTaka(subtotal)}</td>
                    </tr>
                    ${if (discount > 0) """
                    <tr>
                        <td colspan="4" style="text-align: right; padding: 4px; color: #16a34a;">ছাড় (ডিসকাউন্ট):</td>
                        <td style="text-align: right; padding: 4px; font-weight: bold; color: #16a34a;">-${BengaliUtils.formatTaka(discount)}</td>
                    </tr>
                    """ else ""}
                    $previousDueHtml
                    <tr style="border-top: 1px solid #cbd5e1;">
                        <td colspan="4" style="text-align: right; padding: 6px 4px; font-weight: bold; font-size: 13px;">সর্বমোট প্রদেয়:</td>
                        <td style="text-align: right; padding: 6px 4px; font-weight: bold; font-size: 13px; color: #0f172a;">${BengaliUtils.formatTaka(totalPayable)}</td>
                    </tr>
                    <tr style="background: #f0fdf4;">
                        <td colspan="4" style="text-align: right; padding: 6px; font-weight: bold; color: #16a34a;">পরিশোধ (নগদ জমা):</td>
                        <td style="text-align: right; padding: 6px; font-weight: bold; color: #16a34a; font-size: 13px;">${BengaliUtils.formatTaka(paidAmount)}</td>
                    </tr>
                    $currentDueHtml
                </tfoot>
            </table>

            ${if (note.isNotBlank()) "<div style=\"font-size: 11px; color: #64748b; margin-top: 6px;\"><b>নোট:</b> $note</div>" else ""}

            <div class="footer">
                <div>ধন্যবাদ, আবার আসবেন!</div>
                <div style="margin-top: 4px; font-size: 10px; color: #94a3b8;">মাওয়া ডিজিটাল খাতা (MAWA Smart Khata) দ্বারা তৈরি</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Generates a Full Financial Statement Report HTML for Printing / PDF Export.
     */
    fun generateFinancialReportHtml(
        shopName: String,
        shopOwner: String,
        shopPhone: String,
        periodLabel: String,
        dateGenerated: String,
        totalSales: Double,
        cashSales: Double,
        bakiSales: Double,
        bakiCollection: Double,
        totalPurchases: Double,
        shopExpenses: Double,
        homeWithdrawals: Double,
        netProfit: Double,
        profitRemaining: Double,
        transactions: List<TransactionEntity>,
        customersWithDue: List<CustomerWithBalance> = emptyList()
    ): String {
        val txRowsHtml = StringBuilder()
        val dateFormat = SimpleDateFormat("dd MMM, hh:mm a", Locale("bn", "BD"))

        transactions.take(100).forEachIndexed { index, tx ->
            val num = BengaliUtils.toBanglaDigits((index + 1).toLong())
            val dateStr = dateFormat.format(Date(tx.timestamp))
            val typeStr = when (tx.type) {
                TransactionType.SALE_CASH -> "<span style='color:#16a34a; font-weight:bold;'>নগদ বিক্রি</span>"
                TransactionType.SALE_BAKI -> "<span style='color:#dc2626; font-weight:bold;'>বাকি বিক্রি</span>"
                TransactionType.BAKI_COLLECTION -> "<span style='color:#0284c7; font-weight:bold;'>বাকি আদায়</span>"
                TransactionType.PURCHASE_FORDI -> "<span style='color:#ea580c;'>ফর্দ ক্রয়</span>"
                TransactionType.PURCHASE_DIRECT -> "<span style='color:#ea580c;'>সরাসরি ক্রয়</span>"
                TransactionType.EXPENSE_SHOP -> "<span style='color:#ef4444;'>দোকান খরচ</span>"
                TransactionType.EXPENSE_HOME -> "<span style='color:#f59e0b;'>বাড়ি খরচ</span>"
                TransactionType.CASH_ADJUSTMENT -> "<span style='color:#6b7280;'>নগদ সমন্বয়</span>"
            }

            val desc = when {
                !tx.customerName.isNullOrBlank() -> tx.customerName
                !tx.productName.isNullOrBlank() -> tx.productName
                else -> tx.category.ifBlank { "সাধারণ" }
            }

            val amountStr = BengaliUtils.formatTaka(tx.amount)

            txRowsHtml.append("""
                <tr>
                    <td style="text-align:center; padding: 5px; border-bottom: 1px solid #e2e8f0;">$num</td>
                    <td style="padding: 5px; border-bottom: 1px solid #e2e8f0; font-size: 11px;">$dateStr</td>
                    <td style="padding: 5px; border-bottom: 1px solid #e2e8f0;">$typeStr</td>
                    <td style="padding: 5px; border-bottom: 1px solid #e2e8f0;">$desc</td>
                    <td style="text-align:right; padding: 5px; border-bottom: 1px solid #e2e8f0; font-weight:bold;">$amountStr</td>
                </tr>
            """.trimIndent())
        }

        val dueCustomersHtml = if (customersWithDue.isNotEmpty()) {
            val dueRows = StringBuilder()
            customersWithDue.take(15).forEachIndexed { idx, c ->
                dueRows.append("""
                    <tr>
                        <td style="text-align:center; padding: 4px; border-bottom: 1px solid #eee;">${BengaliUtils.toBanglaDigits((idx + 1).toLong())}</td>
                        <td style="padding: 4px; border-bottom: 1px solid #eee; font-weight:bold;">${c.customer.name}</td>
                        <td style="padding: 4px; border-bottom: 1px solid #eee;">${c.customer.phone}</td>
                        <td style="text-align:right; padding: 4px; border-bottom: 1px solid #eee; color:#dc2626; font-weight:bold;">${BengaliUtils.formatTaka(c.currentBalance)}</td>
                    </tr>
                """.trimIndent())
            }
            """
            <div style="margin-top: 20px;">
                <h3 style="font-size: 14px; margin-bottom: 6px; color: #0f172a; border-bottom: 1px solid #cbd5e1; padding-bottom: 4px;">বকেয়া তালিকা (সর্বোচ্চ বাকিদার):</h3>
                <table style="width: 100%; border-collapse: collapse; font-size: 11px;">
                    <thead>
                        <tr style="background: #f8fafc;">
                            <th style="text-align:center; padding: 4px; width: 25px;">নং</th>
                            <th style="text-align:left; padding: 4px;">গ্রাহকের নাম</th>
                            <th style="text-align:left; padding: 4px;">মোবাইল</th>
                            <th style="text-align:right; padding: 4px;">বাকি টাকা</th>
                        </tr>
                    </thead>
                    <tbody>
                        $dueRows
                    </tbody>
                </table>
            </div>
            """.trimIndent()
        } else ""

        val profitColor = if (netProfit >= 0) "#16a34a" else "#dc2626"

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <title>$shopName - $periodLabel লাভ-ক্ষতি ও খতিয়ান রিপোর্ট</title>
            <style>
                @page { size: A4; margin: 15mm; }
                body {
                    font-family: 'SolaimanLipi', 'Kalpurush', 'Hind Siliguri', 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                    background-color: #ffffff;
                    color: #1e293b;
                    margin: 0;
                    padding: 0;
                }
                .header-container {
                    text-align: center;
                    border-bottom: 2px solid #0284c7;
                    padding-bottom: 10px;
                    margin-bottom: 16px;
                }
                .shop-title { font-size: 24px; font-weight: bold; color: #0f172a; margin: 0; }
                .shop-meta { font-size: 13px; color: #475569; margin-top: 2px; }
                .report-badge {
                    display: inline-block;
                    padding: 4px 12px;
                    background: #0284c7;
                    color: #ffffff;
                    border-radius: 20px;
                    font-size: 12px;
                    font-weight: bold;
                    margin-top: 6px;
                }
                .summary-grid {
                    display: grid;
                    grid-template-columns: repeat(3, 1fr);
                    gap: 10px;
                    margin-bottom: 16px;
                }
                .summary-box {
                    border: 1px solid #cbd5e1;
                    border-radius: 8px;
                    padding: 10px;
                    background: #f8fafc;
                }
                .summary-box-title { font-size: 11px; color: #64748b; text-transform: uppercase; font-weight: bold; }
                .summary-box-value { font-size: 16px; font-weight: bold; color: #0f172a; margin-top: 4px; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 10px; }
                th { background: #f1f5f9; padding: 6px; border: 1px solid #cbd5e1; text-align: left; font-weight: bold; }
                td { border: 1px solid #e2e8f0; }
                .highlight-row { background: #f0fdf4; font-weight: bold; }
                .footer {
                    margin-top: 30px;
                    border-top: 1px solid #cbd5e1;
                    padding-top: 10px;
                    font-size: 11px;
                    color: #64748b;
                    display: flex;
                    justify-content: space-between;
                }
            </style>
        </head>
        <body>
            <div class="header-container">
                <h1 class="shop-title">$shopName</h1>
                <div class="shop-meta">প্রোপাইটার: $shopOwner ${if (shopPhone.isNotBlank()) "· মোবাইল: $shopPhone" else ""}</div>
                <div class="report-badge">$periodLabel - লাভ-ক্ষতি ও পূর্ণাঙ্গ হিসাব বিবরণী</div>
                <div style="font-size: 11px; color: #64748b; margin-top: 6px;">তৈরির তারিখ ও সময়: $dateGenerated</div>
            </div>

            <!-- Financial Summary Cards -->
            <div class="summary-grid">
                <div class="summary-box">
                    <div class="summary-box-title">সর্বমোট বিক্রি</div>
                    <div class="summary-box-value">${BengaliUtils.formatTaka(totalSales)}</div>
                    <div style="font-size: 10px; color: #64748b; margin-top: 2px;">নগদ: ${BengaliUtils.formatTaka(cashSales)} | বাকি: ${BengaliUtils.formatTaka(bakiSales)}</div>
                </div>

                <div class="summary-box">
                    <div class="summary-box-title">মাল ক্রয় ও দোকান খরচ</div>
                    <div class="summary-box-value">${BengaliUtils.formatTaka(totalPurchases + shopExpenses)}</div>
                    <div style="font-size: 10px; color: #64748b; margin-top: 2px;">ক্রয়: ${BengaliUtils.formatTaka(totalPurchases)} | খরচ: ${BengaliUtils.formatTaka(shopExpenses)}</div>
                </div>

                <div class="summary-box" style="border-color: $profitColor; background: #fafafa;">
                    <div class="summary-box-title">আনুমানিক নিট লাভ</div>
                    <div class="summary-box-value" style="color: $profitColor;">${BengaliUtils.formatTaka(netProfit)}</div>
                    <div style="font-size: 10px; color: #64748b; margin-top: 2px;">বাড়ির খরচ বাদ দিয়ে: ${BengaliUtils.formatTaka(profitRemaining)}</div>
                </div>
            </div>

            <!-- Complete Table of Financial Breakdown -->
            <table style="margin-bottom: 20px;">
                <thead>
                    <tr>
                        <th colspan="2">হিসাবের প্রধান সূচকসমূহ</th>
                        <th style="text-align: right; width: 120px;">পরিমাণ (৳)</th>
                    </tr>
                </thead>
                <tbody>
                    <tr><td colspan="2" style="padding: 6px;">১. মোট বিক্রি (নগদ বিক্রি + বাকি বিক্রি)</td><td style="text-align: right; padding: 6px; font-weight: bold;">${BengaliUtils.formatTaka(totalSales)}</td></tr>
                    <tr><td style="width: 20px;"></td><td style="padding: 4px; color: #16a34a;">• নগদ বিক্রি</td><td style="text-align: right; padding: 4px; color: #16a34a;">${BengaliUtils.formatTaka(cashSales)}</td></tr>
                    <tr><td></td><td style="padding: 4px; color: #dc2626;">• বাকি বিক্রি</td><td style="text-align: right; padding: 4px; color: #dc2626;">${BengaliUtils.formatTaka(bakiSales)}</td></tr>
                    <tr><td colspan="2" style="padding: 6px; color: #0284c7;">২. বকেয়া বাকি আদায় (জমা)</td><td style="text-align: right; padding: 6px; color: #0284c7; font-weight: bold;">${BengaliUtils.formatTaka(bakiCollection)}</td></tr>
                    <tr><td colspan="2" style="padding: 6px;">৩. মোট মাল ক্রয় (ফর্দ ও সরাসরি ক্রয়)</td><td style="text-align: right; padding: 6px; font-weight: bold; color: #ea580c;">-${BengaliUtils.formatTaka(totalPurchases)}</td></tr>
                    <tr><td colspan="2" style="padding: 6px;">৪. দোকানের পরিচালনা খরচ (ভাড়া, বিদ্যুৎ, বেতন ইত্যাদি)</td><td style="text-align: right; padding: 6px; font-weight: bold; color: #ef4444;">-${BengaliUtils.formatTaka(shopExpenses)}</td></tr>
                    <tr class="highlight-row"><td colspan="2" style="padding: 8px; font-size: 13px;">★ আনুমানিক ব্যবসায়িক নিট লাভ (১ − ৩ − ৪)</td><td style="text-align: right; padding: 8px; font-size: 13px; color: $profitColor;">${BengaliUtils.formatTaka(netProfit)}</td></tr>
                    <tr><td colspan="2" style="padding: 6px; color: #f59e0b;">৫. ব্যক্তিগত/সংসার খরচ (লাভ থেকে উত্তোলন)</td><td style="text-align: right; padding: 6px; color: #f59e0b; font-weight: bold;">-${BengaliUtils.formatTaka(homeWithdrawals)}</td></tr>
                    <tr style="background: #f8fafc; font-weight: bold;"><td colspan="2" style="padding: 6px;">★ সংসারের খরচ বাদ দিয়ে ব্যবসায়ের অবশিষ্ট তহবিল</td><td style="text-align: right; padding: 6px; color: #0284c7;">${BengaliUtils.formatTaka(profitRemaining)}</td></tr>
                </tbody>
            </table>

            $dueCustomersHtml

            <!-- Detailed Transactions Ledger -->
            <h3 style="font-size: 14px; margin-top: 24px; margin-bottom: 6px; color: #0f172a; border-bottom: 1px solid #cbd5e1; padding-bottom: 4px;">লেনদেনের বিস্তারিত অডিট খতিয়ান (${BengaliUtils.toBanglaDigits(transactions.size.toLong())} টি এন্ট্রি):</h3>
            <table>
                <thead>
                    <tr>
                        <th style="width: 25px; text-align: center;">নং</th>
                        <th style="width: 105px;">তারিখ ও সময়</th>
                        <th style="width: 85px;">ধরন</th>
                        <th>বিবরণ / ব্যক্তি / পণ্য</th>
                        <th style="width: 85px; text-align: right;">টাকা</th>
                    </tr>
                </thead>
                <tbody>
                    $txRowsHtml
                </tbody>
            </table>

            <div class="footer">
                <div>হিসাবকারী / ম্যানেজারের স্বাক্ষর: _________________</div>
                <div>MAWA স্মার্ট খাতা অ্যাপ দ্বারা প্রস্তুতকৃত</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Generates a printable HTML report for the full master Baki ledger of all customers.
     */
    fun generateAllCustomersBakiSummaryHtml(
        shopName: String,
        shopOwner: String,
        shopPhone: String,
        customers: List<com.example.mawa.data.model.CustomerWithBalance>,
        totalOutstanding: Double,
        dateGenerated: String
    ): String {
        val totalCustomersWithDue = customers.count { it.currentBalance > 0 }
        val customerRows = StringBuilder()

        customers.sortedByDescending { it.currentBalance }.forEachIndexed { index, item ->
            val cust = item.customer
            val lastTxDate = if (item.lastTransaction != null) BengaliUtils.formatTransactionTime(item.lastTransaction.timestamp) else "লেনদেন নেই"
            val dueColor = if (item.currentBalance > 0) "#dc2626" else "#16a34a"
            val rowBg = if (index % 2 == 0) "#ffffff" else "#f8fafc"
            val limitStr = if (cust.creditLimit > 0) " (সীমা: ${BengaliUtils.formatTaka(cust.creditLimit)})" else ""

            customerRows.append("""
                <tr style="background: $rowBg;">
                    <td style="text-align: center; padding: 6px;">${BengaliUtils.toBanglaDigits((index + 1).toLong())}</td>
                    <td style="padding: 6px; font-weight: bold;">
                        ${cust.name}$limitStr
                        ${if (cust.address.isNotBlank()) "<div style='font-size: 10px; color: #64748b; font-weight: normal;'>${cust.address}</div>" else ""}
                    </td>
                    <td style="padding: 6px;">${if (cust.phone.isNotBlank()) cust.phone else "-"}</td>
                    <td style="padding: 6px; font-size: 11px; color: #64748b;">$lastTxDate</td>
                    <td style="text-align: right; padding: 6px; font-weight: bold; color: $dueColor;">${BengaliUtils.formatTaka(item.currentBalance)}</td>
                </tr>
            """.trimIndent())
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>বাকি খাতা পূর্ণাঙ্গ বিবরণী - $shopName</title>
            <style>
                body { font-family: 'SolaimanLipi', Arial, sans-serif; padding: 20px; color: #1e293b; line-height: 1.4; }
                .header-container { text-align: center; border-bottom: 2px solid #0f766e; padding-bottom: 12px; margin-bottom: 16px; }
                .shop-title { font-size: 22px; font-weight: bold; color: #0f766e; margin: 0; }
                .shop-meta { font-size: 12px; color: #475569; margin-top: 3px; }
                .report-badge { display: inline-block; background: #fee2e2; color: #991b1b; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: bold; margin-top: 8px; }
                .summary-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px; margin-bottom: 16px; }
                .summary-box { border: 1px solid #cbd5e1; border-radius: 8px; padding: 10px; background: #f8fafc; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 10px; }
                th { background: #f1f5f9; padding: 8px 6px; border: 1px solid #cbd5e1; text-align: left; font-weight: bold; }
                td { border: 1px solid #e2e8f0; }
                .footer { margin-top: 30px; border-top: 1px solid #cbd5e1; padding-top: 10px; font-size: 11px; color: #64748b; display: flex; justify-content: space-between; }
            </style>
        </head>
        <body>
            <div class="header-container">
                <h1 class="shop-title">$shopName</h1>
                <div class="shop-meta">প্রোপাইটার: $shopOwner ${if (shopPhone.isNotBlank()) "· মোবাইল: $shopPhone" else ""}</div>
                <div class="report-badge">বাকি খাতা মাস্টার রেজিস্টার ও বকেয়া তালিকা</div>
                <div style="font-size: 11px; color: #64748b; margin-top: 6px;">তৈরির তারিখ ও সময়: $dateGenerated</div>
            </div>

            <div class="summary-grid">
                <div class="summary-box">
                    <div style="font-size: 11px; color: #64748b; font-weight: bold;">মোট বকেয়া বাকি</div>
                    <div style="font-size: 18px; font-weight: bold; color: #dc2626; margin-top: 4px;">${BengaliUtils.formatTaka(totalOutstanding)}</div>
                </div>
                <div class="summary-box">
                    <div style="font-size: 11px; color: #64748b; font-weight: bold;">মোট বাকিদার কাস্টমার</div>
                    <div style="font-size: 18px; font-weight: bold; color: #0f172a; margin-top: 4px;">${BengaliUtils.toBanglaDigits(totalCustomersWithDue.toLong())} জন</div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 30px; text-align: center;">নং</th>
                        <th>কাস্টমার নাম ও ঠিকানা</th>
                        <th style="width: 100px;">মোবাইল</th>
                        <th style="width: 110px;">সর্বশেষ লেনদেন</th>
                        <th style="width: 100px; text-align: right;">বর্তমান বাকি</th>
                    </tr>
                </thead>
                <tbody>
                    $customerRows
                </tbody>
            </table>

            <div class="footer">
                <div>ম্যানেজার / দোকানদারের স্বাক্ষর: _________________</div>
                <div>MAWA স্মার্ট খাতা অ্যাপ দ্বারা প্রস্তুতকৃত</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Generates a printable HTML ledger statement for a single customer.
     */
    fun generateCustomerFullLedgerHtml(
        shopName: String,
        shopOwner: String,
        shopPhone: String,
        customerWithBalance: com.example.mawa.data.model.CustomerWithBalance,
        transactions: List<com.example.mawa.data.local.entity.TransactionEntity>,
        dateGenerated: String
    ): String {
        val cust = customerWithBalance.customer
        val txRows = StringBuilder()

        transactions.sortedBy { it.timestamp }.forEachIndexed { index, tx ->
            val isBaki = tx.type == com.example.mawa.data.local.entity.TransactionType.SALE_BAKI
            val typeStr = if (isBaki) "বাকি প্রদান" else "জমা গ্রহণ"
            val typeColor = if (isBaki) "#dc2626" else "#16a34a"
            val rowBg = if (index % 2 == 0) "#ffffff" else "#f8fafc"

            txRows.append("""
                <tr style="background: $rowBg;">
                    <td style="text-align: center; padding: 6px;">${BengaliUtils.toBanglaDigits((index + 1).toLong())}</td>
                    <td style="padding: 6px;">${BengaliUtils.formatTransactionTime(tx.timestamp)}</td>
                    <td style="padding: 6px; font-weight: bold; color: $typeColor;">$typeStr</td>
                    <td style="padding: 6px;">${tx.note.ifBlank { "-" }}</td>
                    <td style="text-align: right; padding: 6px; color: ${if (isBaki) "#dc2626" else "#64748b"};">${if (isBaki) BengaliUtils.formatTaka(tx.amount) else "-"}</td>
                    <td style="text-align: right; padding: 6px; color: ${if (!isBaki) "#16a34a" else "#64748b"}; font-weight: bold;">${if (!isBaki) BengaliUtils.formatTaka(tx.amount) else "-"}</td>
                </tr>
            """.trimIndent())
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <title>${cust.name} - খতিয়ান বিবরণী</title>
            <style>
                body { font-family: 'SolaimanLipi', Arial, sans-serif; padding: 20px; color: #1e293b; line-height: 1.4; }
                .header-container { text-align: center; border-bottom: 2px solid #0f766e; padding-bottom: 12px; margin-bottom: 16px; }
                .shop-title { font-size: 22px; font-weight: bold; color: #0f766e; margin: 0; }
                .shop-meta { font-size: 12px; color: #475569; margin-top: 3px; }
                .report-badge { display: inline-block; background: #e0f2fe; color: #0369a1; padding: 4px 12px; border-radius: 12px; font-size: 13px; font-weight: bold; margin-top: 8px; }
                .customer-card { border: 1px solid #cbd5e1; border-radius: 8px; padding: 12px; background: #f8fafc; margin-bottom: 16px; }
                table { width: 100%; border-collapse: collapse; font-size: 12px; margin-top: 10px; }
                th { background: #f1f5f9; padding: 8px 6px; border: 1px solid #cbd5e1; text-align: left; font-weight: bold; }
                td { border: 1px solid #e2e8f0; }
                .footer { margin-top: 30px; border-top: 1px solid #cbd5e1; padding-top: 10px; font-size: 11px; color: #64748b; display: flex; justify-content: space-between; }
            </style>
        </head>
        <body>
            <div class="header-container">
                <h1 class="shop-title">$shopName</h1>
                <div class="shop-meta">প্রোপাইটার: $shopOwner ${if (shopPhone.isNotBlank()) "· মোবাইল: $shopPhone" else ""}</div>
                <div class="report-badge">ব্যক্তিগত বাকি খতিয়ান স্টেটমেন্ট</div>
                <div style="font-size: 11px; color: #64748b; margin-top: 6px;">প্রস্তুতের তারিখ: $dateGenerated</div>
            </div>

            <div class="customer-card">
                <div style="display: flex; justify-content: space-between; align-items: center;">
                    <div>
                        <div style="font-size: 16px; font-weight: bold; color: #0f172a;">${cust.name}</div>
                        <div style="font-size: 12px; color: #64748b; margin-top: 2px;">মোবাইল: ${cust.phone.ifBlank { "প্রযোজ্য নয়" }} | ঠিকানা: ${cust.address.ifBlank { "প্রযোজ্য নয়" }}</div>
                        ${if (cust.openingBalance > 0) "<div style='font-size: 11px; color: #64748b; margin-top: 2px;'>সাবেক/পূর্বের প্রারম্ভিক বাকি: ${BengaliUtils.formatTaka(cust.openingBalance)}</div>" else ""}
                    </div>
                    <div style="text-align: right;">
                        <div style="font-size: 11px; color: #64748b; font-weight: bold;">বর্তমান বকেয়া বাকি</div>
                        <div style="font-size: 20px; font-weight: bold; color: #dc2626;">${BengaliUtils.formatTaka(customerWithBalance.currentBalance)}</div>
                    </div>
                </div>
            </div>

            <table>
                <thead>
                    <tr>
                        <th style="width: 30px; text-align: center;">নং</th>
                        <th style="width: 110px;">তারিখ</th>
                        <th style="width: 80px;">ধরন</th>
                        <th>বিবরণ</th>
                        <th style="width: 90px; text-align: right;">বাকি (৳)</th>
                        <th style="width: 90px; text-align: right;">জমা (৳)</th>
                    </tr>
                </thead>
                <tbody>
                    $txRows
                </tbody>
            </table>

            <div class="footer">
                <div>গ্রাহকের স্বাক্ষর: _________________</div>
                <div>দোকানদারের স্বাক্ষর: _________________</div>
            </div>
        </body>
        </html>
        """.trimIndent()
    }

    /**
     * Formatted string for Bluetooth Thermal POS printers (ESC/POS text format).
     */
    fun generateThermalPosString(
        shopName: String,
        shopPhone: String,
        memoNo: String,
        dateFormatted: String,
        customerName: String?,
        items: List<InvoiceItem>,
        totalAmount: Double,
        paidAmount: Double,
        dueAmount: Double
    ): String {
        val sb = StringBuilder()
        sb.appendLine("================================")
        sb.appendLine("        $shopName")
        if (shopPhone.isNotBlank()) sb.appendLine("     মোবাইল: $shopPhone")
        sb.appendLine("================================")
        sb.appendLine("মেমো নং: $memoNo")
        sb.appendLine("তারিখ: $dateFormatted")
        if (!customerName.isNullOrBlank()) {
            sb.appendLine("গ্রাহক: $customerName")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("পণ্য              পরিমাণ    মোট ")
        sb.appendLine("--------------------------------")
        items.forEach { item ->
            val nameShort = if (item.name.length > 14) item.name.substring(0, 12) + ".." else item.name.padEnd(14)
            val qtyStr = (if (item.quantity > 0) "${item.quantity}" else "1").padEnd(6)
            val amtStr = BengaliUtils.formatTaka(item.amount).padStart(8)
            sb.appendLine("$nameShort $qtyStr $amtStr")
        }
        sb.appendLine("--------------------------------")
        sb.appendLine("মোট টাকা:         ${BengaliUtils.formatTaka(totalAmount).padStart(12)}")
        sb.appendLine("পরিশোধ (জমা):     ${BengaliUtils.formatTaka(paidAmount).padStart(12)}")
        if (dueAmount > 0) {
            sb.appendLine("বকেয়া (বাকি):      ${BengaliUtils.formatTaka(dueAmount).padStart(12)}")
        }
        sb.appendLine("================================")
        sb.appendLine("      ধন্যবাদ, আবার আসবেন!       ")
        sb.appendLine("      মাওয়া স্মার্ট ডিজিটাল খাতা      ")
        sb.appendLine("================================")
        sb.appendLine("\n\n")
        return sb.toString()
    }
}

data class InvoiceItem(
    val name: String,
    val quantity: Double = 1.0,
    val unit: String = "কেজি",
    val rate: Double = 0.0,
    val amount: Double
)

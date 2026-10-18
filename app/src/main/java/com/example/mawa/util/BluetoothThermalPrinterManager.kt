package com.example.mawa.util

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStream
import java.nio.charset.Charset
import java.util.UUID

data class BluetoothPrinterDevice(
    val name: String,
    val address: String,
    val isLikelyPrinter: Boolean = false
)

object BluetoothThermalPrinterManager {

    // Standard SPP UUID for Serial Bluetooth printers (RFCOMM)
    private val SPP_UUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private const val PREFS_NAME = "mawa_app_prefs"
    private const val KEY_PRINTER_ADDR = "preferred_bt_printer_address"
    private const val KEY_PRINTER_NAME = "preferred_bt_printer_name"

    fun getBluetoothAdapter(context: Context): BluetoothAdapter? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val bm = context.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager
            bm?.adapter ?: BluetoothAdapter.getDefaultAdapter()
        } else {
            @Suppress("DEPRECATION")
            BluetoothAdapter.getDefaultAdapter()
        }
    }

    fun isBluetoothEnabled(context: Context): Boolean {
        return try {
            getBluetoothAdapter(context)?.isEnabled == true
        } catch (_: SecurityException) {
            false
        }
    }

    @SuppressLint("MissingPermission")
    fun getPairedPrinters(context: Context): List<BluetoothPrinterDevice> {
        val adapter = getBluetoothAdapter(context) ?: return emptyList()
        return try {
            val devices = adapter.bondedDevices ?: return emptyList()
            devices.map { device ->
                val name = device.name ?: "Unknown Device"
                val addr = device.address ?: ""
                val devClass = device.bluetoothClass?.majorDeviceClass ?: 0
                val isPrinter = name.contains("print", ignoreCase = true) ||
                        name.contains("pos", ignoreCase = true) ||
                        name.contains("rpp", ignoreCase = true) ||
                        name.contains("mpt", ignoreCase = true) ||
                        name.contains("thermal", ignoreCase = true) ||
                        name.contains("58", ignoreCase = true) ||
                        name.contains("80", ignoreCase = true) ||
                        devClass == 1536 || devClass == 1664 // Imaging / peripheral
                BluetoothPrinterDevice(
                    name = name,
                    address = addr,
                    isLikelyPrinter = isPrinter
                )
            }.sortedWith(compareByDescending<BluetoothPrinterDevice> { it.isLikelyPrinter }.thenBy { it.name })
        } catch (_: SecurityException) {
            emptyList()
        }
    }

    fun getPreferredPrinter(context: Context): Pair<String?, String?> {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val addr = prefs.getString(KEY_PRINTER_ADDR, null)
        val name = prefs.getString(KEY_PRINTER_NAME, null)
        return Pair(addr, name)
    }

    fun setPreferredPrinter(context: Context, address: String, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_PRINTER_ADDR, address)
            .putString(KEY_PRINTER_NAME, name)
            .apply()
    }

    fun clearPreferredPrinter(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .remove(KEY_PRINTER_ADDR)
            .remove(KEY_PRINTER_NAME)
            .apply()
    }

    /**
     * Connects to the Bluetooth Thermal printer and sends ESC/POS byte payload
     */
    @SuppressLint("MissingPermission")
    suspend fun sendBytesToPrinter(
        context: Context,
        deviceAddress: String,
        payload: ByteArray
    ): Result<Unit> = withContext(Dispatchers.IO) {
        val adapter = getBluetoothAdapter(context)
            ?: return@withContext Result.failure(Exception("ফোনে ব্লুটুথ সাপোর্ট করে না"))

        if (!adapter.isEnabled) {
            return@withContext Result.failure(Exception("ফোনের ব্লুটুথ অন (চালু) করুন"))
        }

        var socket: BluetoothSocket? = null
        var outputStream: OutputStream? = null

        try {
            val device: BluetoothDevice = adapter.getRemoteDevice(deviceAddress)
            // Cancel discovery to increase connection speed and stability
            adapter.cancelDiscovery()

            socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
            socket.connect()
            outputStream = socket.outputStream

            outputStream.write(payload)
            outputStream.flush()

            // Small wait to ensure buffer is dispatched before socket closure
            kotlinx.coroutines.delay(200)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("প্রিন্টারে কানেক্ট করা যায়নি: ${e.localizedMessage}"))
        } finally {
            try { outputStream?.close() } catch (_: Exception) {}
            try { socket?.close() } catch (_: Exception) {}
        }
    }

    /**
     * Builds ESC/POS bytes for 58mm / 80mm thermal receipt
     */
    fun buildThermalReceiptEscPos(
        shopName: String,
        shopPhone: String,
        memoNo: String,
        dateFormatted: String,
        customerName: String?,
        items: List<InvoiceItem>,
        totalAmount: Double,
        paidAmount: Double,
        dueAmount: Double,
        is80mm: Boolean = false
    ): ByteArray {
        val bytes = mutableListOf<Byte>()

        fun add(vararg b: Int) {
            b.forEach { bytes.add(it.toByte()) }
        }

        fun addText(text: String) {
            val b = text.toByteArray(Charset.forName("UTF-8"))
            b.forEach { bytes.add(it) }
        }

        val lineWidth = if (is80mm) 42 else 32

        // Initialize printer
        add(0x1B, 0x40)

        // Center Align
        add(0x1B, 0x61, 0x01)

        // Double Height + Bold for Shop Name
        add(0x1B, 0x45, 0x01) // Bold on
        add(0x1D, 0x21, 0x11) // 2x Height and Width
        addText(shopName + "\n")

        // Reset text size
        add(0x1D, 0x21, 0x00)
        add(0x1B, 0x45, 0x00) // Bold off

        if (shopPhone.isNotBlank()) {
            addText("মোবাইল: $shopPhone\n")
        }
        addText("=".repeat(lineWidth) + "\n")

        // Left Align
        add(0x1B, 0x61, 0x00)
        addText("মেমো নং: $memoNo\n")
        addText("তারিখ  : $dateFormatted\n")
        if (!customerName.isNullOrBlank()) {
            addText("গ্রাহক : $customerName\n")
        }
        addText("-".repeat(lineWidth) + "\n")

        // Table Header
        val colNameWidth = if (is80mm) 22 else 15
        val colQtyWidth = if (is80mm) 8 else 6
        val colTotalWidth = if (is80mm) 12 else 9

        val header = "পণ্য".padEnd(colNameWidth) + "পরিমাণ".padEnd(colQtyWidth) + "মোট".padStart(colTotalWidth)
        add(0x1B, 0x45, 0x01)
        addText(header + "\n")
        add(0x1B, 0x45, 0x00)
        addText("-".repeat(lineWidth) + "\n")

        // Items
        items.forEach { item ->
            val cleanName = if (item.name.length > colNameWidth - 1) {
                item.name.take(colNameWidth - 2) + "."
            } else {
                item.name.padEnd(colNameWidth)
            }
            val qtyStr = (if (item.quantity > 0) "${item.quantity}" else "1").padEnd(colQtyWidth)
            val amtStr = BengaliUtils.formatTaka(item.amount).padStart(colTotalWidth)
            addText("$cleanName$qtyStr$amtStr\n")
        }

        addText("-".repeat(lineWidth) + "\n")

        // Summary Calculations (Right aligned / Indented)
        add(0x1B, 0x45, 0x01)
        val totalLine = "মোট টাকা:".padEnd(lineWidth - 12) + BengaliUtils.formatTaka(totalAmount).padStart(12)
        addText(totalLine + "\n")

        val paidLine = "পরিশোধ/জমা:".padEnd(lineWidth - 12) + BengaliUtils.formatTaka(paidAmount).padStart(12)
        addText(paidLine + "\n")

        if (dueAmount > 0) {
            val dueLine = "বকেয়া বাকি:".padEnd(lineWidth - 12) + BengaliUtils.formatTaka(dueAmount).padStart(12)
            addText(dueLine + "\n")
        }
        add(0x1B, 0x45, 0x00)

        addText("=".repeat(lineWidth) + "\n")

        // Footer Note (Center)
        add(0x1B, 0x61, 0x01)
        addText("ধন্যবাদ, আবার আসবেন!\n")
        addText("মাওয়া স্মার্ট ডিজিটাল খাতা\n")

        // Feed lines and paper cut
        addText("\n\n\n")
        add(0x1D, 0x56, 0x42, 0x00) // Cut Paper

        return bytes.toByteArray()
    }

    /**
     * Test print ESC/POS bytes to verify printer connectivity
     */
    fun buildTestPrintEscPos(shopName: String): ByteArray {
        val bytes = mutableListOf<Byte>()
        fun add(vararg b: Int) { b.forEach { bytes.add(it.toByte()) } }
        fun addText(t: String) { t.toByteArray(Charset.forName("UTF-8")).forEach { bytes.add(it) } }

        add(0x1B, 0x40) // Reset
        add(0x1B, 0x61, 0x01) // Center
        add(0x1B, 0x45, 0x01) // Bold
        add(0x1D, 0x21, 0x11) // 2x
        addText(shopName + "\n")
        add(0x1D, 0x21, 0x00)
        add(0x1B, 0x45, 0x00)
        addText("================================\n")
        addText("★ ব্লুটুথ থার্মাল প্রিন্টার টেস্ট ★\n")
        addText("মাওয়া স্মার্ট খাতা সফলভাবে কানেক্ট হয়েছে!\n")
        addText("তারিখ: ${java.text.SimpleDateFormat("dd/MM/yyyy hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())}\n")
        addText("================================\n")
        addText("প্রিন্টার মডেল: 58mm / 80mm POS\n")
        addText("স্ট্যাটাস: সক্রিয় (Ready)\n\n\n\n")
        add(0x1D, 0x56, 0x42, 0x00) // Cut

        return bytes.toByteArray()
    }
}

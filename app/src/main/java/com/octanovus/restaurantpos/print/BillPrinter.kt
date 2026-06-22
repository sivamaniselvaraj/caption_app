package com.octanovus.restaurantpos.print

import android.content.Context
import com.dantsu.escposprinter.EscPosPrinter
import com.dantsu.escposprinter.connection.DeviceConnection
import com.dantsu.escposprinter.connection.bluetooth.BluetoothPrintersConnections
import com.dantsu.escposprinter.connection.tcp.TcpConnection
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Resolves the right connection from PrinterSettings and prints formatted ESC/POS text.
 * Supports Bluetooth, TCP (network/WiFi) and USB. Runs entirely off the main thread.
 */
class BillPrinter(private val context: Context) {

    suspend fun print(formattedText: String) = withContext(Dispatchers.IO) {
        val s = _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings
        val connection: DeviceConnection = when (s.type) {
            _root_ide_package_.com.octanovus.restaurantpos.print.PrinterType.BLUETOOTH ->
                BluetoothPrintersConnections.selectFirstPaired()
                    ?: error("No paired Bluetooth printer found")
            _root_ide_package_.com.octanovus.restaurantpos.print.PrinterType.TCP ->
                TcpConnection(s.ip, s.port)
            _root_ide_package_.com.octanovus.restaurantpos.print.PrinterType.USB ->
                UsbPrintersConnections.selectFirstConnected(context)
                    ?: error("No USB printer connected")
        }
        EscPosPrinter(connection, s.dpi, s.paperWidthMM, s.charsPerLine)
            .printFormattedTextAndCut(formattedText)
            .disconnectPrinter()
    }
}

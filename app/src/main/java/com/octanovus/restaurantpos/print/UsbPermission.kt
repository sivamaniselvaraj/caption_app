package com.octanovus.restaurantpos.print

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.dantsu.escposprinter.connection.usb.UsbPrintersConnections

const val ACTION_USB = "com.octanovus.restaurantpos.USB_PERMISSION"

/**
 * Request permission for the first connected USB printer (only needed for USB).
 * Register a BroadcastReceiver for ACTION_USB to learn when the user accepts; after
 * that, printing works for the session.
 */
fun requestUsbPermission(context: Context) {
    val usb = UsbPrintersConnections.selectFirstConnected(context) ?: return
    val pi = PendingIntent.getBroadcast(
        context, 0, Intent(_root_ide_package_.com.octanovus.restaurantpos.print.ACTION_USB), PendingIntent.FLAG_IMMUTABLE
    )
    val manager = context.getSystemService(Context.USB_SERVICE) as android.hardware.usb.UsbManager
    manager.requestPermission(usb.device, pi)
}

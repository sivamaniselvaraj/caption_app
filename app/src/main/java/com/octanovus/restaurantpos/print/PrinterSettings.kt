package com.octanovus.restaurantpos.print

import android.content.Context
import androidx.core.content.edit

enum class PrinterType { BLUETOOTH, TCP, USB }

/**
 * SharedPreferences-backed printer settings. Call init() once from Application.onCreate().
 * 58 mm paper -> width 48f, 32 chars/line. 80 mm paper -> width 72f, 48 chars/line.
 */
object PrinterSettings {
    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("printer", Context.MODE_PRIVATE)
    }

    var type: com.octanovus.restaurantpos.print.PrinterType
        get() = _root_ide_package_.com.octanovus.restaurantpos.print.PrinterType.valueOf(prefs.getString("type", _root_ide_package_.com.octanovus.restaurantpos.print.PrinterType.BLUETOOTH.name)!!)
        set(v) = prefs.edit { putString("type", v.name) }

    var ip: String
        get() = prefs.getString("ip", "192.168.0.100")!!
        set(v) = prefs.edit { putString("ip", v) }

    var port: Int
        get() = prefs.getInt("port", 9100)
        set(v) = prefs.edit { putInt("port", v) }

    var dpi: Int
        get() = prefs.getInt("dpi", 203)
        set(v) = prefs.edit { putInt("dpi", v) }

    var paperWidthMM: Float
        get() = prefs.getFloat("paper", 48f)
        set(v) = prefs.edit { putFloat("paper", v) }

    var charsPerLine: Int
        get() = prefs.getInt("cpl", 32)
        set(v) = prefs.edit { putInt("cpl", v) }

    var restaurantName: String
        get() = prefs.getString("name", "My Restaurant")!!
        set(v) = prefs.edit { putString("name", v) }
}

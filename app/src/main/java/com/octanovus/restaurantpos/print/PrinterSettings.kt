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

    var host: String
        get() = prefs.getString("host", "192.168.0.100")!!
        set(v) = prefs.edit { putString("host", v) }

    var port: Int
        get() = prefs.getInt("port", 5001)
        set(v) = prefs.edit { putInt("port", v) }

    var restaurantName: String
        get() = prefs.getString("name", "My Restaurant")!!
        set(v) = prefs.edit { putString("name", v) }
}

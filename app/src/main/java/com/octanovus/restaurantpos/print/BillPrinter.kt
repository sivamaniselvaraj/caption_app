package com.octanovus.restaurantpos.print

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URL

/**
 * Triggers a bill print by calling the print server:
 *   POST http://{host}:{port}/api/order
 * The server is responsible for fetching the order and rendering/printing it.
 */
class BillPrinter {

    suspend fun printOrder(orderId: String, type: String) = withContext(Dispatchers.IO) {
        val s = PrinterSettings
        // Send the order id in the JSON body.
        val payload = """{"orderId":"$orderId"}"""
        val url = URL("http://${s.host}:${s.port}/api/print-order?type=$type")
        Log.d("BillPrinter", "POST $url  body=$payload")
        // Proxy.NO_PROXY bypasses any Wi-Fi proxy configured on the device, which
        // can block direct LAN connections even when the browser works.
        val conn = (url.openConnection(Proxy.NO_PROXY) as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            doOutput = true
            useCaches = false
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
            setRequestProperty("Accept", "application/json")
        }
        try {
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val body = (conn.errorStream ?: conn.inputStream)
                    ?.bufferedReader()?.use { it.readText() }
                    .orEmpty()
                error("Print server returned HTTP $code" + if (body.isNotBlank()) ": ${body.take(200)}" else "")
            }
        } catch (e: IOException) {
            // Network-level failure: never reached the server.
            Log.e("BillPrinter", "connect failed to $url", e)
            throw IOException("Could not reach print server at ${s.host}:${s.port} — ${e.message}", e)
        } finally {
            conn.disconnect()
        }
    }
}

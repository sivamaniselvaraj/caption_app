package com.octanovus.restaurantpos.print

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Triggers a bill print by calling the print server:
 *   POST http://{host}:{port}/api/order
 * The server is responsible for fetching the order and rendering/printing it.
 */
class BillPrinter {

    suspend fun printOrder(orderId: String) = withContext(Dispatchers.IO) {
        val s = PrinterSettings
        val url = URL("http://${s.host}:${s.port}/api/print-order")
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }
        try {
            // Send the order id in the JSON body.
            val payload = """{"orderId":"$orderId"}"""
            conn.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
            val code = conn.responseCode
            if (code !in 200..299) {
                val body = (conn.errorStream ?: conn.inputStream)
                    ?.bufferedReader()?.use { it.readText() }
                    .orEmpty()
                error("Print server returned HTTP $code" + if (body.isNotBlank()) ": ${body.take(200)}" else "")
            }
        } finally {
            conn.disconnect()
        }
    }
}

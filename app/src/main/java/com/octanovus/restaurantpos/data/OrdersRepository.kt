package com.octanovus.restaurantpos.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.request.RpcRequestBuilder
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.OffsetDateTime

class OrdersRepository {
    private val pg get() = Supabase.client.postgrest

    suspend fun activeOrder(tableId: String): Order? =
        pg.from("orders").select {
            filter {
                eq("table_id", tableId)
                isIn("status", listOf("open", "confirmed"))
            }
        }.decodeList<Order>().firstOrNull()

    suspend fun itemsFor(orderId: String): List<OrderItem> =
        pg.from("order_items").select {
            filter { eq("order_id", orderId) }
        }.decodeList()

    suspend fun createOrder(tableId: String, orderNumber: String, userId: String?): Order =
        pg.from("orders")
            .insert(NewOrder(tableId = tableId, orderNumber = orderNumber, createdBy = userId, outletId = Session.profile?.outletId, createdAt = OffsetDateTime.now().toString())) { select() }
            .decodeSingle()

    suspend fun addItems(items: List<NewOrderItem>) {
        if (items.isNotEmpty()) pg.from("order_items").insert(items)
    }

    suspend fun getOrderNumber(outletId: String?): String {
        return pg.rpc("get_next_order_number", buildJsonObject{put("p_outlet_id", outletId)}).data
    }

    suspend fun confirm(
        orderId: String,
        tableId: String,
        subtotal: Double,
        tax: Double,
        total: Double
    ) {
        pg.from("orders").update({
            set("status", "confirmed")
            set("subtotal", subtotal)
            set("tax_amount", tax)
            set("total", total)
            set("updated_at", OffsetDateTime.now().toString())
        }) { filter { eq("id", orderId) } }

        pg.from("restaurant_tables").update({
            set("status", "occupied")
        }) { filter { eq("id", tableId) } }
    }

    suspend fun markPaid(orderId: String, tableId: String) {
        pg.from("orders").update({
            set("status", "paid")
            set("paid_at", OffsetDateTime.now().toString())
        }) { filter { eq("id", orderId) } }

        pg.from("restaurant_tables").update({
            set("status", "free")
        }) { filter { eq("id", tableId) } }
    }

    /** Atomic move + table-status update handled server-side by the transfer_order function. */
    suspend fun transfer(orderId: String, toTableId: String) {
        pg.rpc("transfer_order", buildJsonObject {
            put("p_order_id", orderId)
            put("p_to_table", toTableId)
        })
    }
}

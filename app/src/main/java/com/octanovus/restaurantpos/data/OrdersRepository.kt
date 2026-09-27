package com.octanovus.restaurantpos.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.serialization.json.addJsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import java.time.OffsetDateTime

class OrdersRepository {
    private val pg get() = Supabase.client.postgrest

    /** Statuses that count as "active" for a dine-in table (not yet printed/paid). */
    private val activeStatuses =
        listOf("open", "confirmed", "pending", "preparing", "ready", "served")

    /** All active (unpaid, un-printed) orders for a table. A table can now have many. */
    suspend fun activeOrders(tableId: String): List<Order> =
        pg.from("orders").select {
            filter {
                eq("table_id", tableId)
                isIn("status", activeStatuses)
            }
        }.decodeList()

    /** Items across a set of orders (used to show every active order on a table). */
    suspend fun itemsForOrders(orderIds: List<String>): List<OrderItem> {
        if (orderIds.isEmpty()) return emptyList()
        return pg.from("order_items").select(
            Columns.raw("id, order_id, menu_item_id, unit_price, quantity, total_price, status, menu_items:menu_items(name)")
        ) {
            filter { isIn("order_id", orderIds) }
        }.decodeList()
    }

    /** Items of a single order. */
    suspend fun itemsFor(orderId: String): List<OrderItem> =
        pg.from("order_items").select (
            Columns.raw("id, order_id, menu_item_id, unit_price, quantity, total_price, status, menu_items:menu_items(name)")
        ) {
            filter { eq("order_id", orderId) }
        }.decodeList()

    /**
     * Creates a NEW order for the table (server-side place_order always inserts a
     * fresh order now), inserts its items, and keeps the table occupied — atomically.
     * Returns the new order id.
     */
    suspend fun placeOrder(
        tableId: String,
        outletId: String?,
        items: List<OrderItemInput>,
        subtotal: Double,
        tax: Double,
        total: Double,
        userId: String?
    ): String {

        val placeOrderParams = buildJsonObject {
            put("p_table_id", tableId)
            put("p_outlet_id", outletId)
            putJsonArray("p_items") {
                items.forEach { item ->
                    addJsonObject {
                        put("menu_item_id", item.menuItemId)
                        //put("name", item.name)
                        put("unit_price", item.unitPrice)
                        put("total_price", item.unitPrice * item.quantity)
                        put("quantity", item.quantity)
                    }
                }
            }
            put("p_subtotal", subtotal)
            put("p_tax", tax)
            put("p_total", total)
            put("p_created_by", userId)
        }
        return pg.rpc(
            "place_order",
            placeOrderParams,
        ).decodeAs<String>()
    }

    /**
     * Bill printed: mark ALL of the table's active orders as payment_pending.
     * Deliberately does NOT change the table status — the desktop app frees it.
     */
    suspend fun markTablePaymentPending(tableId: String) {
        pg.from("orders").update({
            set("status", "payment_pending")
        }) {
            filter {
                eq("table_id", tableId)
                isIn("status", activeStatuses)
            }
        }
    }

    /** Atomic move + table-status update handled server-side by transfer_order. */
    suspend fun transfer(orderId: String, toTableId: String) {
        pg.rpc("transfer_order", buildJsonObject {
            put("p_order_id", orderId)
            put("p_to_table", toTableId)
        })
    }
}
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

    suspend fun activeOrder(tableId: String): Order? =
        pg.from("orders").select {
            filter {
                eq("table_id", tableId)
                isIn("status", listOf("open", "confirmed", "pending", "preparing", "ready", "served"))
            }
        }.decodeList<Order>().firstOrNull()

    suspend fun itemsFor(orderId: String): List<OrderItem> =
        pg.from("order_items").select ( Columns.raw("id, order_id, menu_item_id, unit_price, quantity, total_price, status, menu_items:menu_items(name)") ){
            filter { eq("order_id", orderId) }
        }.decodeList()


    /**
     * Creates (or appends to) the table's order, inserts the line items, marks the
     * order confirmed and the table occupied — all inside ONE Postgres transaction.
     * If any step fails, the server rolls the entire thing back, so there are no
     * orphan orders or half-updated tables. Returns the order id.
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


    suspend fun markPaid(orderId: String, tableId: String) {
        pg.from("orders").update({
            set("status", "completed")
            set("paid_at", OffsetDateTime.now().toString())
        }) { filter { eq("id", orderId) } }

        pg.from("tables").update({
            set("status", "available")
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

package com.octanovus.restaurantpos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonIgnoreUnknownKeys
import java.util.Date

// ---- Read models ----

@Serializable
data class Profiles(
    val id: String,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("first_name") val firstName: String? = null,
    val role: String = "waiter",
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("outlet_id") val outletId: String? = null,
)

@Serializable
data class RestaurantTable(
    val id: String,
    //val label: String,
    @SerialName("capacity") val capacity: Int = 4,
    val status: String = "available",
    @SerialName("table_number") val tableNumber: String? = null,
    @SerialName("outlet_id") val outletId: String,
    @SerialName("floor_area") val floorArea: String? = null,
)

@Serializable
//@JsonIgnoreUnknownKeys
data class MenuCategory(
    val id: String,
    val name: String,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("outlet_id") val outletId: String,
    @SerialName("is_active") val isActive: Boolean = true
)

@Serializable
@JsonIgnoreUnknownKeys
data class MenuItem(
    val id: String,
    @SerialName("category_id") val categoryId: String? = null,
    val name: String,
    val price: Double,
    @SerialName("cooking_time") val cookingTime: Int = 0,
    @SerialName("is_available") val isAvailable: Boolean = true,
    @SerialName("sort_order") val sortOrder: Int = 0,
    @SerialName("is_active") val isActive: Boolean = true,
    @SerialName("container_charge") val containerCharge: Int = 0,
    @SerialName("outlet_id") val outletId: String,
    @SerialName("search_key") val searchKey: String = ""

)

@Serializable
@JsonIgnoreUnknownKeys
data class Order(
    val id: String,
    @SerialName("outlet_id") val outletId: String,
    @SerialName("order_type") val orderType: String = "dine_in",
    @SerialName("order_number") val orderNumber: String,
    val subtotal: Double = 0.0,
    @SerialName("tax_amount") val taxAmount : Double = 0.0,
    @SerialName("waiter_id") val waiterId: String,
    @SerialName("created_at") val createdAt: String = Date().toString(),
    @SerialName("updated_at") val updatedAt: String = Date().toString(),
    @SerialName("table_id") val tableId: String,
    val status: String,
    val total: Double = 0.0
)

@Serializable
@JsonIgnoreUnknownKeys
data class OrderItem(
    val id: String,
    @SerialName("order_id") val orderId: String,
    //val name: String? = null,
    @SerialName("menu_item_id") val menuItemsId: String,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("total_price") val totalPrice: Double,
    val quantity: Int,
    val status: String? = "pending",
    val notes: String? = null,
    @SerialName("menu_items") val item: MenuItemRef? = null
){
    /** Live name from the joined menu_items row, falling back to the stored snapshot. */
   // val displayName: String? get() = item?.name ?: name
}


// ---- Insert DTOs (omit DB-generated columns) ----

/** One line item sent to the place_order RPC (no order_id — the function assigns it). */
@Serializable
data class OrderItemInput(
    @SerialName("menu_item_id") val menuItemId: String,
    val name: String,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("total_price") val totalPrice: Double,
    val quantity: Int
)

/** The slice of the joined menu_items row we embed into order_items reads. */
@Serializable
data class MenuItemRef(val name: String? = null)
package com.octanovus.restaurantpos.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
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
//@JsonIgnoreUnknownKeys
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
data class OrderItem(
    val id: String,
    @SerialName("order_id") val orderId: String,
    val name: String,
    @SerialName("menu_item_id") val menuItemsId: String,
    @SerialName("unit_price") val unitPrice: Double,
    @SerialName("total_price") val totalPrice: Double,
    val quantity: Int,
    val notes: String? = null
)

// ---- Insert DTOs (omit DB-generated columns) ----


@Serializable
data class NewOrder(
    @SerialName("table_id") val tableId: String,
    @SerialName("order_number") val orderNumber: String,
    @SerialName("outlet_id") val outletId: String?,
    @SerialName("waiter_id") val createdBy: String? = null,
    @SerialName("created_at") val createdAt: String? = null,
    val status: String = "open"
)

@Serializable
data class NewOrderItem(
    @SerialName("order_id") val orderId: String,
    @SerialName("menu_item_id") val menuItemsId: String,
    val name: String,
    @SerialName("unit_price") val unitPrice: Double,
    val quantity: Int,
    @SerialName("total_price") val totalPrice: Double,
    val notes: String? = null
)

package com.octanovus.restaurantpos.ui.order

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octanovus.restaurantpos.data.AuthRepository
import com.octanovus.restaurantpos.data.MenuCategory
import com.octanovus.restaurantpos.data.MenuItem
import com.octanovus.restaurantpos.data.MenuRepository
import com.octanovus.restaurantpos.data.OrderItem
import com.octanovus.restaurantpos.data.OrderItemInput
import com.octanovus.restaurantpos.data.OrdersRepository
import com.octanovus.restaurantpos.data.RestaurantTable
import com.octanovus.restaurantpos.data.Session
import com.octanovus.restaurantpos.data.TablesRepository
import kotlinx.coroutines.launch

const val TAX_RATE = 0.05   // e.g. 0.05 for 5%
const val CURRENCY = "₹"

data class CartLine(val item: MenuItem, val qty: Int)

class OrderViewModel(
    private val tableId: String,
    private val menuRepo: MenuRepository = MenuRepository(),
    private val ordersRepo: OrdersRepository = OrdersRepository(),
    private val tablesRepo: TablesRepository = TablesRepository(),
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    var categories by mutableStateOf<List<MenuCategory>>(emptyList()); private set
    var menu by mutableStateOf<List<MenuItem>>(emptyList()); private set
    var selectedCategory by mutableStateOf<String?>(null)
    var searchQuery by mutableStateOf("")
    var existing by mutableStateOf<List<OrderItem>>(emptyList()); private set
    var cart by mutableStateOf<Map<String, CartLine>>(emptyMap()); private set
    var loading by mutableStateOf(true); private set
    var confirming by mutableStateOf(false); private set
    var working by mutableStateOf(false); private set
    var error by mutableStateOf<String?>(null)

    var tableLabel by mutableStateOf(""); private set
    var freeTables by mutableStateOf<List<RestaurantTable>>(emptyList()); private set

    private var orderId: String? = null
    val hasActiveOrder get() = orderId != null
    val activeOrderId: String? get() = orderId

    init { load() }

    private fun load() = viewModelScope.launch {
        try {
            categories = menuRepo.categories()
            menu = menuRepo.items()
            selectedCategory = categories.firstOrNull()?.id
            val order = ordersRepo.activeOrder(tableId)
            orderId = order?.id
            existing = order?.let { ordersRepo.itemsFor(it.id) } ?: emptyList()
            val all = tablesRepo.getTables()
            tableLabel = all.firstOrNull { it.id == tableId }?.tableNumber ?: ""
            freeTables = all.filter { it.id != tableId && it.status == "available" }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    val isSearching get() = searchQuery.isNotBlank()

    /**
     * When a search term is present, match item names across ALL categories;
     * otherwise show the items in the selected category tab.
     */
    fun visibleItems(): List<MenuItem> {
        val q = searchQuery.trim()
        return if (q.isBlank()) menu.filter { it.categoryId == selectedCategory }
        else menu.filter { it.name.contains(q, ignoreCase = true) || it.searchKey.contains(q, ignoreCase = true)}
    }
    fun qtyOf(id: String) = cart[id]?.qty ?: 0

    fun add(item: MenuItem) {
        cart = cart + (item.id to CartLine(item, (cart[item.id]?.qty ?: 0) + 1))
    }

    fun remove(item: MenuItem) {
        val q = (cart[item.id]?.qty ?: 0) - 1
        cart = if (q <= 0) cart - item.id else cart + (item.id to CartLine(item, q))
    }

    val existingSubtotal get() = existing.sumOf { it.unitPrice * it.quantity }
    val cartSubtotal get() = cart.values.sumOf { it.item.price * it.qty }
    val cartCount get() = existing.sumOf { it.quantity } + cart.values.sumOf { it.qty }
    val subtotal get() = existingSubtotal + cartSubtotal
    val tax get() = subtotal * TAX_RATE
    val total get() = subtotal + tax

    /** Persists the current cart as confirmed items, then clears it. */
    fun confirm(onDone: () -> Unit) = viewModelScope.launch {
        if (cart.isEmpty()) { onDone(); return@launch }
        confirming = true; error = null
        try {
            val items = cart.values.map {
                OrderItemInput(it.item.id, it.item.name, it.item.price, it.item.price * it.qty, it.qty)
            }

            // One atomic server call: order + items + table status, or nothing.
            val id = ordersRepo.placeOrder(
                tableId = tableId,
                outletId = Session.profile?.outletId,
                items = items,
                subtotal = subtotal,
                tax = tax,
                total = total,
                userId = auth.currentUserId
            )
            orderId = id
            // Success only: fold the just-saved cart into the existing lines.
            existing = existing + cart.values.map {
                OrderItem(
                    id = "tmp-${it.item.id}", orderId = id,
                    unitPrice = it.item.price, quantity = it.qty,
                    menuItemsId = it.item.id,
                    totalPrice = it.item.price * it.qty,
                )
            }
            cart = emptyMap()
            onDone()
        } catch (e: Exception) {
            error = e.message
        } finally {
            confirming = false
        }
    }

    fun markPaid(onDone: () -> Unit) = viewModelScope.launch {
        val id = orderId ?: return@launch
        working = true
        try {
            ordersRepo.markPaid(id, tableId)
            onDone()
        } catch (e: Exception) {
            error = e.message
        } finally {
            working = false
        }
    }

    fun transfer(toTableId: String, onDone: () -> Unit) = viewModelScope.launch {
        val id = orderId ?: return@launch
        working = true
        try {
            ordersRepo.transfer(id, toTableId)
            onDone()
        } catch (e: Exception) {
            error = e.message
        } finally {
            working = false
        }
    }
}

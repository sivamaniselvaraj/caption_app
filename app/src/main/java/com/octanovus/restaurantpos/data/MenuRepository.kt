package com.octanovus.restaurantpos.data

import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns

class MenuRepository {
    private val pg get() = Supabase.client.postgrest

    suspend fun categories(): List<MenuCategory> =
        pg.from("categories").select(Columns.list("id", "outlet_id", "name", "sort_order","is_active")){
        filter { eq("is_active", true); eq("outlet_id", Session.profile?.outletId ?: "")}
        }.decodeList<MenuCategory>().sortedBy { it.sortOrder }

    suspend fun items(): List<MenuItem> =
        pg.from("menu_items").select (Columns.list("id", "category_id",
            "name",  "outlet_id", "price", "cooking_time", "container_charge","sort_order")){
            filter { eq("is_available", true); eq("outlet_id", Session.profile?.outletId ?: "")}
        }.decodeList<MenuItem>().sortedBy { it.sortOrder }
}

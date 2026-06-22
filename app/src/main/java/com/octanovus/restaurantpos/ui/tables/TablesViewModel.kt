package com.octanovus.restaurantpos.ui.tables

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octanovus.restaurantpos.data.TablesRepository
import com.octanovus.restaurantpos.data.RestaurantTable
import kotlinx.coroutines.launch

class TablesViewModel(
    private val repo: TablesRepository = TablesRepository()
) : ViewModel() {

    var tables by mutableStateOf<List<RestaurantTable>>(emptyList()); private set
    var loading by mutableStateOf(true); private set
    var error by mutableStateOf<String?>(null); private set

    init {
        load()
        //observeRealtime() //uncomment for realtime sync
    }

    fun load() = viewModelScope.launch {
        try {
            tables = repo.getTables().sortedBy { it.tableNumber }
            error = null
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    private fun observeRealtime() = viewModelScope.launch {
        try {
            repo.tableChanges().collect { load() } // re-fetch on any change
        } catch (_: Exception) {
            // Realtime is optional; manual refresh still works.
        }
    }
}

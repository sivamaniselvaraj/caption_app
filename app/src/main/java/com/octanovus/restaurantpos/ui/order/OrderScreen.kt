package com.octanovus.restaurantpos.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.octanovus.restaurantpos.data.MenuItem
import com.octanovus.restaurantpos.print.BillPrinter
import kotlinx.coroutines.launch

private fun money(v: Double) = CURRENCY + String.format("%.2f", v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(tableId: String, onBack: () -> Unit, onViewOrder: () -> Unit) {
    val vm: OrderViewModel = viewModel(
        factory = viewModelFactory { initializer { OrderViewModel(tableId) } }
    )

    val scope = rememberCoroutineScope()
    val printer = remember { BillPrinter() }
    var menuOpen by remember { mutableStateOf(false) }

    fun doPayAndPrint() {
        vm.confirm {                       // persist any new cart lines + totals first
            scope.launch {
                try {
                    vm.activeOrderId?.let { printer.printOrder(it) }
                    vm.markPaid(onBack)
                } catch (e: Exception) {
                    vm.error = e.message
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (vm.tableLabel.isBlank()) "Order" else "Table ${vm.tableLabel}") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (vm.hasActiveOrder) {
                        IconButton(onClick = { menuOpen = true }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            Text(
                                "Transfer to",
                                Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                            vm.freeTables.forEach { t ->
                                DropdownMenuItem(text = { Text("Table - " + t.tableNumber) }, onClick = {
                                    menuOpen = false
                                    vm.transfer(t.id, onBack)
                                })
                            }
                            if (vm.freeTables.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("No free tables") },
                                    onClick = {},
                                    enabled = false
                                )
                            }
                        }
                    }
                }
            )
        },
        bottomBar = {
            Surface(tonalElevation = 3.dp) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Items ${vm.cartCount}", fontWeight = FontWeight.SemiBold)
                        Text("Total ${money(vm.total)}", fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = onViewOrder,
                        enabled = vm.cart.isNotEmpty() || vm.existing.isNotEmpty(),
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("View order") }
                    if (vm.hasActiveOrder) {
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { doPayAndPrint() },
                            enabled = !vm.working && !vm.confirming,
                            modifier = Modifier.fillMaxWidth()
                        ) { Text("Pay & print bill") }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                vm.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                else -> Column(Modifier.fillMaxSize()) {
                    OutlinedTextField(
                        value = vm.searchQuery,
                        onValueChange = { vm.searchQuery = it },
                        placeholder = { Text("Search menu") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        trailingIcon = {
                            if (vm.isSearching) {
                                IconButton(onClick = { vm.searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    )

                    // Category tabs only matter when not searching (search spans all categories).
                    if (!vm.isSearching && vm.categories.isNotEmpty()) {
                        val selectedIndex = vm.categories.indexOfFirst { it.id == vm.selectedCategory }
                        ScrollableTabRow(
                            selectedTabIndex = selectedIndex.coerceAtLeast(0),
                            edgePadding = 12.dp
                        ) {
                            vm.categories.forEach { cat ->
                                Tab(
                                    selected = cat.id == vm.selectedCategory,
                                    onClick = { vm.selectedCategory = cat.id },
                                    text = { Text(cat.name) }
                                )
                            }
                        }
                    }

                    val shown = vm.visibleItems()
                    if (shown.isEmpty() && vm.isSearching) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No items match \"${vm.searchQuery.trim()}\"",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(Modifier.weight(1f)) {
                            items(shown, key = { it.id }) { item ->
                                MenuRow(item, vm.qtyOf(item.id), { vm.add(item) }, { vm.remove(item) })
                                HorizontalDivider()
                            }
                        }
                    }

                    vm.error?.let {
                        Text(
                            "Error: $it",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MenuRow(item: MenuItem, qty: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.bodyLarge)
            Text(
                money(item.price),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (qty > 0) {
            OutlinedIconButton(onClick = onRemove) { Text("-") }
            Text("$qty", Modifier.padding(horizontal = 10.dp), fontWeight = FontWeight.SemiBold)
        }
        FilledIconButton(onClick = onAdd) { Text("+") }
    }
}
package com.octanovus.restaurantpos.ui.order

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private fun money(v: Double) = CURRENCY + String.format("%.2f", v)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewOrderScreen(
    vm: OrderViewModel,
    onBack: () -> Unit,
    onConfirmed: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (vm.tableLabel.isBlank()) "View order" else "Order · Table ${vm.tableLabel}")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
                        onClick = { vm.confirm(onDone = onConfirmed) },
                        enabled = vm.cart.isNotEmpty() && !vm.confirming && !vm.working,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (vm.confirming)
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp)
                        else Text("Confirm order")
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            if (vm.cart.isEmpty() && vm.existing.isEmpty()) {
                Text(
                    "No items selected yet.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(Modifier.fillMaxSize()) {
                    if (vm.existing.isNotEmpty()) {
                        item { SectionHeader("Already ordered") }
                        items(
                            vm.existing,
                            key = { it.id }) { line ->
                            ExistingRow(line.item?.name.toString(), line.quantity, line.unitPrice * line.quantity)
                            HorizontalDivider()
                        }
                    }
                    if (vm.cart.isNotEmpty()) {
                        item { SectionHeader("New items") }
                        items(
                            vm.cart.values.toList(),
                            key = { it.item.id }) { line ->
                            CartRow(
                                name = line.item.name,
                                qty = line.qty,
                                lineTotal = line.item.price * line.qty,
                                onAdd = { vm.add(line.item) },
                                onRemove = { vm.remove(line.item) }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}

@Composable
private fun ExistingRow(name: String, qty: Int, lineTotal: Double) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            "$qty x",
            Modifier.padding(end = 10.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(name, Modifier.weight(1f))
        Text(money(lineTotal))
    }
}

@Composable
private fun CartRow(
    name: String,
    qty: Int,
    lineTotal: Double,
    onAdd: () -> Unit,
    onRemove: () -> Unit
) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.bodyLarge)
            Text(
                money(lineTotal),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        OutlinedIconButton(onClick = onRemove) { Text("-") }
        Text("$qty", Modifier.padding(horizontal = 10.dp), fontWeight = FontWeight.SemiBold)
        FilledIconButton(onClick = onAdd) { Text("+") }
    }
}

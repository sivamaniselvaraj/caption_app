package com.octanovus.restaurantpos.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.octanovus.restaurantpos.print.PrinterSettings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var host by remember { mutableStateOf(PrinterSettings.host) }
    var port by remember { mutableStateOf(PrinterSettings.port.toString()) }
    var saved by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier.padding(pad).fillMaxSize()
                .verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Print server", style = MaterialTheme.typography.labelLarge)

            OutlinedTextField(
                host, { host = it; saved = false },
                label = { Text("Host / IP") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                port, { port = it.filter(Char::isDigit); saved = false },
                label = { Text("Port") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                "Bills print via POST http://${host.ifBlank { "host" }}:${port.ifBlank { "port" }}/api/order/{orderId}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = {
                    PrinterSettings.host = host.trim()
                    PrinterSettings.port = port.toIntOrNull() ?: 5001
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }

            if (saved) Text("Saved", color = MaterialTheme.colorScheme.primary)
        }
    }
}
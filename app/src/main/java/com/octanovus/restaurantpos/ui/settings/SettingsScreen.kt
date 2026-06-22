package com.octanovus.restaurantpos.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.octanovus.restaurantpos.print.PrinterSettings
import com.octanovus.restaurantpos.print.PrinterType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit) {
    var name by remember { mutableStateOf(_root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.restaurantName) }
    var type by remember { mutableStateOf(_root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.type) }
    var ip by remember { mutableStateOf(_root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.ip) }
    var port by remember { mutableStateOf(_root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.port.toString()) }
    var paper by remember { mutableStateOf(_root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.paperWidthMM.toString()) }
    var cpl by remember { mutableStateOf(_root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.charsPerLine.toString()) }
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
            OutlinedTextField(
                name, { name = it; saved = false },
                label = { Text("Restaurant name") },
                modifier = Modifier.fillMaxWidth()
            )

            Text("Printer connection", style = MaterialTheme.typography.labelLarge)
            Column {
                _root_ide_package_.com.octanovus.restaurantpos.print.PrinterType.entries.forEach { t ->
                    Row(
                        Modifier.fillMaxWidth()
                            .selectable(selected = type == t, onClick = { type = t; saved = false })
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = type == t, onClick = { type = t; saved = false })
                        Spacer(Modifier.width(8.dp))
                        Text(t.name)
                    }
                }
            }

            if (type == _root_ide_package_.com.octanovus.restaurantpos.print.PrinterType.TCP) {
                OutlinedTextField(
                    ip, { ip = it; saved = false },
                    label = { Text("Printer IP") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    port, { port = it.filter(Char::isDigit); saved = false },
                    label = { Text("Port") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Text("Paper size", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { paper = "48.0"; cpl = "32"; saved = false },
                    label = { Text("58 mm") }
                )
                AssistChip(
                    onClick = { paper = "72.0"; cpl = "48"; saved = false },
                    label = { Text("80 mm") }
                )
            }
            OutlinedTextField(
                paper, { paper = it; saved = false },
                label = { Text("Paper width (mm)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                cpl, { cpl = it.filter(Char::isDigit); saved = false },
                label = { Text("Characters per line") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = {
                    _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.restaurantName = name.trim()
                    _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.type = type
                    _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.ip = ip.trim()
                    _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.port = port.toIntOrNull() ?: 9100
                    _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.paperWidthMM = paper.toFloatOrNull() ?: 48f
                    _root_ide_package_.com.octanovus.restaurantpos.print.PrinterSettings.charsPerLine = cpl.toIntOrNull() ?: 32
                    saved = true
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }

            if (saved) Text("Saved", color = MaterialTheme.colorScheme.primary)
        }
    }
}

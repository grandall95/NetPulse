package page.gagerandall.netpulse.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val theme by viewModel.themeState.collectAsState()
    val dnsVal by viewModel.dnsState.collectAsState()
    val portLimit by viewModel.portConcurrencyState.collectAsState()

    var showDnsDialog by remember { mutableStateOf(false) }
    var tempDnsVal by remember { mutableStateOf("") }

    var showAboutDialog by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Text(
            text = "Settings & Specifications",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Theme Toggle Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "Theme Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Theme Selection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("System", "Light", "Dark").forEach { option ->
                        val isSelected = theme == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTheme(option) },
                            label = { Text(text = option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        // Port Scanner Limit
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Port Scanner Concurrency",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Current maximum simultaneous probes: $portLimit threads",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Slider(
                    value = portLimit.toFloat(),
                    onValueChange = { viewModel.setPortConcurrency(it.toInt()) },
                    valueRange = 10f..200f,
                    steps = 19
                )
            }
        }

        // DNS Prefs
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable {
                    tempDnsVal = dnsVal
                    showDnsDialog = true
                },
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Dns,
                    contentDescription = "DNS Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Custom DNS Server",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Active host: ${if (dnsVal == "system") "System Default" else dnsVal}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "Edit")
            }
        }

        // About / Open Source Licenses
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable { showAboutDialog = true },
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "About & Licenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "NetPulse v1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "View Info")
            }
        }
    }

    // Custom DNS Edit Dialog
    if (showDnsDialog) {
        AlertDialog(
            onDismissRequest = { showDnsDialog = false },
            title = { Text("Set Custom DNS Server") },
            text = {
                Column {
                    Text("Provide custom hostname or IP address (e.g. 1.1.1.1, 8.8.8.8) or type 'system' to revert.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = tempDnsVal,
                        onValueChange = { tempDnsVal = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("system, 8.8.8.8, 1.1.1.1") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.setDefaultDns(tempDnsVal.trim().lowercase())
                    showDnsDialog = false
                }) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDnsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Open Source Libraries Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "Open Source Integration Credits",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .height(350.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "This toolkit is compiled using the following open-source libraries:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // DnsJava
                    Text("DnsJava (Version 3.6.5)", fontWeight = FontWeight.Bold)
                    Text("High performance, custom DNS packet resolver & parser.\nLicense: BSD 3-Clause\n", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // OkHttp
                    Text("Square OkHttp (Version 4.12.0)", fontWeight = FontWeight.Bold)
                    Text("Advanced connection pooling, header inspection, and automated TLS/SSL client APIs.\nLicense: Apache 2.0\n", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Accompanist Permissions
                    Text("Google Accompanist Permissions (Version 0.37.0)", fontWeight = FontWeight.Bold)
                    Text("Idiomatic Jetpack Compose runtime permission workflows.\nLicense: Apache 2.0\n", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Datastore
                    Text("Androidx Jetpack DataStore (Version 1.1.3)", fontWeight = FontWeight.Bold)
                    Text("Type-safe, persistent local preferences backed by Kotlin coroutines.\nLicense: Apache 2.0\n", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    // Jetpack Compose
                    Text("Jetpack Compose Material 3", fontWeight = FontWeight.Bold)
                    Text("Declarative, interactive UI layouts built with Google's M3 specifications.\nLicense: Apache 2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Developed by Gage Randall",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://www.gagerandall.page/contact") }
                            .padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Built with Open Source in Mind: GNU General Public License version 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://opensource.org/license/GPL-3.0") }
                            .padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

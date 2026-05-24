package com.example.ui.screens.portscan

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ResultCard
import com.example.ui.theme.ColorExcellent
import com.example.ui.theme.ColorFailed
import com.example.ui.theme.ColorGood
import com.example.ui.theme.ColorPoor

@Composable
fun PortScannerScreen(viewModel: PortScannerViewModel) {
    val state by viewModel.state.collectAsState()

    var hostInput by remember { mutableStateOf("127.0.0.1") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Simple, 1: Advanced

    // Presets
    var selectedPreset by remember { mutableStateOf("Common") }

    // Advanced inputs
    var customPortsInput by remember { mutableStateOf("21, 22, 80, 443, 8080") }
    var threadLimitInput by remember { mutableStateOf("50") }
    var timeoutInput by remember { mutableStateOf("200") }

    val scrollState = rememberScrollState()

    val commonPorts = listOf(21, 22, 23, 25, 53, 80, 110, 143, 443, 445, 3389, 8080)
    val webPorts = listOf(80, 443, 8080, 8443)
    val mailPorts = listOf(25, 465, 587, 110, 995, 143, 993)
    val dbPorts = listOf(1433, 1521, 3306, 5432, 27017, 6379)
    val wellKnownPorts = (1..100).toList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "TCP Port Scanner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Tab selection
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("General User", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Power User", fontWeight = FontWeight.SemiBold) }
            )
        }

        // Host Input
        OutlinedTextField(
            value = hostInput,
            onValueChange = { hostInput = it },
            label = { Text("Target IP Address / Local Domain") },
            placeholder = { Text("e.g. 192.168.1.1 or localhost") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (state.status == "Running") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Preset Selector on Simple Tab
        if (selectedTab == 0) {
            Text("Port Selection Profile", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                val presets = listOf("Common", "Web", "Mail", "Database", "Well-Known")
                presets.forEach { opt ->
                    FilterChip(
                        selected = selectedPreset == opt,
                        onClick = { selectedPreset = opt },
                        label = { Text(opt, fontSize = 11.sp) },
                        modifier = Modifier.padding(horizontal = 2.dp)
                    )
                }
            }
        } else {
            // Advanced Inputs Frame
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Advanced Port Testing Specs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = customPortsInput,
                        onValueChange = { customPortsInput = it },
                        label = { Text("Custom Port Range (comma-separated or dash e.g. 1-100)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = threadLimitInput,
                            onValueChange = { threadLimitInput = it },
                            label = { Text("Concurrency limit") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = timeoutInput,
                            onValueChange = { timeoutInput = it },
                            label = { Text("Timeout per port (ms)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            singleLine = true
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // DISCLAIMER BANNER (MANDATORY CONSTRAINT)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Authorized Network Diagnostics Policy",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = "Disclaimer: Scanning networks you do not own or have written permission to inspect may violate local statutes. Use responsibly.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (state.status == "Running") {
            Button(
                onClick = { viewModel.stopScan() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Stop")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Port Security Review")
            }
        } else {
            Button(
                onClick = {
                    val ports = if (selectedTab == 0) {
                        when (selectedPreset) {
                            "Common" -> commonPorts
                            "Web" -> webPorts
                            "Mail" -> mailPorts
                            "Database" -> dbPorts
                            "Well-Known" -> wellKnownPorts
                            else -> commonPorts
                        }
                    } else {
                        parseCustomPortsRange(customPortsInput)
                    }

                    val limit = threadLimitInput.toIntOrNull() ?: 50
                    val timeout = timeoutInput.toIntOrNull() ?: 200

                    viewModel.startScan(
                        host = hostInput,
                        portsToScan = ports,
                        timeoutMs = timeout,
                        concurrencyLimit = limit
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Execute Authorized Port Security Review")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Scanner running progress
        if (state.status == "Running") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Scanning ports sequence: ${(state.progress * 100).toInt()}% completed",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = state.progress,
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        if (state.status == "Failed") {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(text = state.errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
            }
        }

        // Render port results
        if (state.status != "Idle" && state.status != "Failed") {
            ResultCard(
                title = "Diagnostic Port Map: $hostInput",
                statusText = if (state.status == "Running") "Scanning..." else "Complete",
                statusColor = if (state.status == "Running") ColorGood else ColorExcellent
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active query duration", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.elapsedMs} ms", fontWeight = FontWeight.Bold)
                    }
                    val openCount = state.scannedPorts.count { it.status == PortScannerViewModel.PortStatus.OPEN }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Open interfaces", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$openCount discovered", fontWeight = FontWeight.Bold, color = if (openCount > 0) ColorExcellent else ColorGood)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                if (state.scannedPorts.isEmpty()) {
                    Text("Aligning socket parameters...", style = MaterialTheme.typography.bodyMedium)
                } else {
                    state.scannedPorts.forEach { res ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Port ", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(res.port.toString(), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(res.service, fontSize = 12.sp, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                            }
                            
                            val (badgeTxt, badgeCol) = when (res.status) {
                                PortScannerViewModel.PortStatus.OPEN -> Pair("OPEN", ColorExcellent)
                                PortScannerViewModel.PortStatus.CLOSED -> Pair("CLOSED", Color.Gray)
                                PortScannerViewModel.PortStatus.FILTERED -> Pair("FILTERED", ColorPoor)
                            }

                            Box(
                                modifier = Modifier
                                    .background(badgeCol.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(badgeTxt, color = badgeCol, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Convert comma-separated string like "22,80,443" or range like "1-100" into Integer Ports List
private fun parseCustomPortsRange(input: String): List<Int> {
    val result = mutableListOf<Int>()
    try {
        val clean = input.trim().replace(" ", "")
        if (clean.contains("-")) {
            val parts = clean.split("-")
            if (parts.size == 2) {
                val start = parts[0].toIntOrNull() ?: 1
                val end = parts[1].toIntOrNull() ?: 100
                return (start.coerceAtLeast(1)..end.coerceAtMost(65535)).toList()
            }
        } else {
            clean.split(",").forEach { segment ->
                segment.toIntOrNull()?.let { result.add(it) }
            }
        }
    } catch (_: Exception) {}
    if (result.isEmpty()) return listOf(22, 80, 443)
    return result
}

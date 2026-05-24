package com.example.ui.screens.dns

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ResultCard
import com.example.ui.theme.ColorExcellent
import com.example.ui.theme.ColorFailed
import com.example.ui.theme.ColorGood
import com.example.ui.theme.ColorPoor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DnsLookupScreen(viewModel: DnsLookupViewModel) {
    val state by viewModel.state.collectAsState()

    var domainInput by remember { mutableStateOf("google.com") }
    var selectedType by remember { mutableStateOf("A") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Simple, 1: Advanced

    var useCustomServer by remember { mutableStateOf(false) }
    var customServerInput by remember { mutableStateOf("1.1.1.1") }

    val recordTypes = listOf("A", "AAAA", "MX", "TXT", "CNAME", "NS", "PTR")
    var dropdownExpanded by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "DNS Record Resolver",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = {
                    selectedTab = 0
                    useCustomServer = false
                },
                text = { Text("General User", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = {
                    selectedTab = 1
                    useCustomServer = true
                },
                text = { Text("Power User", fontWeight = FontWeight.SemiBold) }
            )
        }

        OutlinedTextField(
            value = domainInput,
            onValueChange = { domainInput = it },
            label = { Text("Domain Name") },
            placeholder = { Text("e.g. google.com") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (state.status == "Running") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Record type Dropdown field
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedType,
                onValueChange = { },
                readOnly = true,
                label = { Text("Record DNS Type") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { dropdownExpanded = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select")
                    }
                }
            )
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                recordTypes.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            selectedType = type
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Custom DNS Server choice on advanced tab
        if (selectedTab == 1) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Dns, contentDescription = "DNS Server", tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Query Custom Server", fontWeight = FontWeight.Bold)
                        }
                        Switch(
                            checked = useCustomServer,
                            onCheckedChange = { useCustomServer = it }
                        )
                    }

                    AnimatedVisibility(visible = useCustomServer) {
                        Column {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = customServerInput,
                                onValueChange = { customServerInput = it },
                                label = { Text("DNS Server IP") },
                                placeholder = { Text("e.g. 1.1.1.1") },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Resolver presets:", style = MaterialTheme.typography.labelMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth()) {
                                listOf(
                                    Pair("Cloudflare", "1.1.1.1"),
                                    Pair("Google", "8.8.8.8"),
                                    Pair("Quad9", "9.9.9.9")
                                ).forEach { pair ->
                                    ElevatedButton(
                                        onClick = { customServerInput = pair.second },
                                        modifier = Modifier
                                            .padding(end = 6.dp)
                                            .height(34.dp),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(pair.first, fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                viewModel.lookupDns(
                    domain = domainInput,
                    recordTypeStr = selectedType,
                    useCustomServer = useCustomServer,
                    customServerIp = customServerInput
                )
            },
            enabled = state.status != "Running",
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Resolve Network Records")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.status == "Failed") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Resolution Failure: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Render Results List
        if (state.status != "Idle" && state.status != "Failed") {
            ResultCard(
                title = "Resolve results for $domainInput ($selectedType)",
                statusText = if (state.status == "Running") "Resolving..." else "Complete",
                statusColor = if (state.status == "Running") ColorGood else ColorExcellent
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Response speed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.responseTimeMs} ms", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = ColorGood)
                    }
                    if (useCustomServer) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Authoritative reply", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = if (state.authoritativeFlag) "Yes" else "No",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge,
                                color = if (state.authoritativeFlag) ColorExcellent else ColorPoor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                if (state.records.isEmpty() && state.status == "Complete") {
                    Text(
                        text = "No compatible '$selectedType' records resolved for this domain.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    state.records.forEach { record ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                .padding(10.dp)
                        ) {
                            Text(
                                text = record.value,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (record.ttl > 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("TTL (Cache lifetime)", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text("${record.ttl}s", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                if (selectedTab == 1 && state.status == "Complete") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Raw DNS Packet Response", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = state.rawResponse,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}

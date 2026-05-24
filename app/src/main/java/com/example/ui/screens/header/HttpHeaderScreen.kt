package com.example.ui.screens.header

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
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
fun HttpHeaderScreen(viewModel: HttpHeaderViewModel) {
    val state by viewModel.state.collectAsState()

    var urlInput by remember { mutableStateOf("google.com") }
    var selectedMethod by remember { mutableStateOf("GET") }
    var selectedTab by remember { mutableStateOf(0) } // 0: Simple, 1: Advanced

    var dropdownExpanded by remember { mutableStateOf(false) }

    // Advanced inputs
    var followRedirectsManually by remember { mutableStateOf(false) }
    val customHeaders = remember { mutableStateListOf(HttpHeaderViewModel.HeaderPair("User-Agent", "NetPulse-Diagnostics-v1")) }
    var newHeaderKey by remember { mutableStateOf("") }
    var newHeaderValue by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "HTTP Header Analyst",
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
                onClick = { selectedTab = 0 },
                text = { Text("General User", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Power User", fontWeight = FontWeight.SemiBold) }
            )
        }

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            label = { Text("Destination URL / Core Domain") },
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

        // Selected Method dropdown
        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = selectedMethod,
                onValueChange = { },
                readOnly = true,
                label = { Text("Inspection Method") },
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { dropdownExpanded = true }) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Expand")
                    }
                }
            )
            DropdownMenu(
                expanded = dropdownExpanded,
                onDismissRequest = { dropdownExpanded = false },
                modifier = Modifier.fillMaxWidth(0.9f)
            ) {
                listOf("GET", "HEAD").forEach { method ->
                    DropdownMenuItem(
                        text = { Text(method, style = MaterialTheme.typography.bodyMedium) },
                        onClick = {
                            selectedMethod = method
                            dropdownExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Advanced Configuration
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
                        Text("Follow Redirects Manually", fontWeight = FontWeight.Bold)
                        Switch(
                            checked = followRedirectsManually,
                            onCheckedChange = { followRedirectsManually = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Injected Client Request Headers", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))

                    customHeaders.forEachIndexed { index, header ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${header.key}: ${header.value}", fontSize = 12.sp, modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { customHeaders.removeAt(index) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newHeaderKey,
                            onValueChange = { newHeaderKey = it },
                            placeholder = { Text("Key", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newHeaderValue,
                            onValueChange = { newHeaderValue = it },
                            placeholder = { Text("Value", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            singleLine = true
                        )
                        IconButton(
                            onClick = {
                                if (newHeaderKey.isNotBlank()) {
                                    customHeaders.add(HttpHeaderViewModel.HeaderPair(newHeaderKey.trim(), newHeaderValue.trim()))
                                    newHeaderKey = ""
                                    newHeaderValue = ""
                                }
                            },
                            modifier = Modifier.background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.onPrimary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = {
                viewModel.inspectUrl(
                    urlInput = urlInput,
                    method = selectedMethod,
                    customHeaders = customHeaders.toList(),
                    followRedirectsManually = followRedirectsManually
                )
            },
            enabled = state.status != "Running",
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Analyze Endpoint Protocol Headers")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.status == "Failed") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Connection Failed: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Render Results Frame
        if (state.status != "Idle" && state.status != "Failed") {
            val statusColor = when (state.statusCode) {
                in 200..299 -> ColorExcellent
                in 300..399 -> ColorPoor
                else -> ColorFailed
            }

            ResultCard(
                title = "Endpoint Header Response Details",
                statusText = "status: ${state.statusCode}",
                statusColor = statusColor
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Response speed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.responseTimeMs} ms", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge, color = ColorGood)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("SSL Secure Handshake", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = if (state.sslInfo != null) "Active TLS" else "Insecure / HTTP",
                            fontWeight = FontWeight.Bold,
                            color = if (state.sslInfo != null) ColorExcellent else ColorFailed
                        )
                    }
                }

                // Follow Redirect chain logs
                if (followRedirectsManually && state.redirectChain.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Manual Redirect Hops", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    state.redirectChain.forEachIndexed { index, node ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("${index + 1}. Status ${node.status}", fontSize = 11.sp)
                            Text(node.url, fontSize = 11.sp, maxLines = 1, modifier = Modifier.padding(horizontal = 8.dp).weight(1f))
                            Text("${node.elapsedMs}ms", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                // SSL Card info
                if (selectedTab == 1 && state.sslInfo != null) {
                    state.sslInfo?.let { ssl ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = "SSL certificate details", tint = ColorExcellent, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("TLS Security Certificate Information", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("Subject: ${ssl.subject}", fontSize = 11.sp, lineHeight = 14.sp)
                                Text("Issuer: ${ssl.issuer}", fontSize = 11.sp, lineHeight = 14.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text("Valid From: ${ssl.validFrom}", fontSize = 11.sp)
                                Text("Valid To: ${ssl.validTo}", fontSize = 11.sp)
                            }
                        }
                    }
                }

                // Response headers Key Values Map
                Text("HTTP Response Headers Block", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(bottom = 6.dp))
                state.responseHeaders.forEach { header ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
                            .padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = "${header.key}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.width(120.dp),
                            lineHeight = 14.sp
                        )
                        Text(
                            text = header.value,
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.weight(1f),
                            lineHeight = 14.sp
                        )
                    }
                }
            }
        }
    }
}

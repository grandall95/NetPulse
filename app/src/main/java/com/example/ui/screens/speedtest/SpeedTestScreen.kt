package com.example.ui.screens.speedtest

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.LatencyChart
import com.example.ui.components.ResultCard
import com.example.ui.theme.ColorExcellent
import com.example.ui.theme.ColorFailed
import com.example.ui.theme.ColorGood
import com.example.ui.theme.ColorPoor

@Composable
fun SpeedTestScreen(viewModel: SpeedTestViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Simple, 1: Advanced

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(
            text = "Internet Speed Analyzer",
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
                .padding(bottom = 20.dp)
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

        // Action launch button
        Button(
            onClick = { viewModel.runSpeedTest() },
            enabled = state.status != "Measuring Latency..." && state.status != "Downloading..." && state.status != "Uploading...",
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Speed, contentDescription = "Run Test")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Begin Cloudflare Speed Probe")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Running process banner
        if (state.status != "Idle" && state.status != "Complete" && state.status != "Failed") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Current State: ${state.status}",
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Failed state
        if (state.status == "Failed") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Text(
                    text = "Probe Error: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Diagnostic visual results card
        if (state.status != "Idle") {
            ResultCard(
                title = "Diagnostic Speed Log",
                statusText = if (state.status == "Complete") "Complete" else "Working...",
                statusColor = if (state.status == "Complete") ColorExcellent else ColorGood
            ) {
                // Latency and location core fields
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Server PoP: ${state.serverLocation} (Colo IP: ${state.ipAddress})",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Exchange Latency RTT", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${state.latencyMs.toInt()} ms",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (state.latencyMs < 50f) ColorExcellent else if (state.latencyMs < 150f) ColorGood else ColorPoor
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Active Interface", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Cloudflare Edge", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(16.dp))

                // Download section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudDownload, contentDescription = "Download", tint = ColorExcellent)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download Bandwidth Throughput", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", state.downloadMbps),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorExcellent
                    )
                    Text(" Mbps", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
                }
                LinearProgressIndicator(
                    progress = state.downloadProgress,
                    color = ColorExcellent,
                    trackColor = ColorExcellent.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.Transparent, RoundedCornerShape(3.dp))
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Upload section
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Upload", tint = ColorGood)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Upload Bandwidth Throughput", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = String.format("%.2f", state.uploadMbps),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = ColorGood
                    )
                    Text(" Mbps", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(bottom = 6.dp))
                }
                LinearProgressIndicator(
                    progress = state.uploadProgress,
                    color = ColorGood,
                    trackColor = ColorGood.copy(alpha = 0.2f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .background(Color.Transparent, RoundedCornerShape(3.dp))
                )

                // Advanced metrics listing
                if (selectedTab == 1) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Text("Telemetry Specifications", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bytes Transferred (Down)")
                        Text("${String.format("%.2f", state.bytesDownloaded / 1_000_000f)} MB", fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Bytes Transferred (Up)")
                        Text("${String.format("%.2f", state.bytesUploaded / 1_000_000f)} MB", fontWeight = FontWeight.SemiBold)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Test Sequence Duration")
                        Text("${String.format("%.1f", state.durationSec)}s", fontWeight = FontWeight.SemiBold)
                    }

                    // Dynamically render speeds graph
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Flow Chart Throughput (Real-Time Sparkline)", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(bottom = 6.dp))
                    LatencyChart(dataPoints = state.realTimeSpeeds, modifier = Modifier.fillMaxWidth(), enableZoom = true)
                }
            }
        }
    }
}

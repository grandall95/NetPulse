package com.example.ui.screens.wifi

import android.Manifest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.PermissionRationaleDialog
import com.example.ui.components.ResultCard
import com.example.ui.theme.ColorExcellent
import com.example.ui.theme.ColorFailed
import com.example.ui.theme.ColorGood
import com.example.ui.theme.ColorPoor
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun WifiAnalyzerScreen(viewModel: WifiAnalyzerViewModel) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: Simple, 1: Advanced

    val context = LocalContext.current
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    )

    var showPermissionRationale by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    // Synergize state permissions
    LaunchedEffect(locationPermissionState.status.isGranted) {
        viewModel.setPermissionGranted(locationPermissionState.status.isGranted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Wi-Fi Channel Space",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Network Studio • RF interference detection",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // General / Advanced Tab Selector
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

        // Location permission warning notice
        if (!locationPermissionState.status.isGranted) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Access Fine Location required",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = "In Android 10+ location services must be active to scan nearby wifi beacon details.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(onClick = {
                        if (locationPermissionState.status.isGranted) {
                            // nothing
                        } else {
                            showPermissionRationale = true
                        }
                    }) {
                        Text("Grant", fontSize = 10.sp)
                    }
                }
            }
        }

        // Control section row (Manual Refresh + Continuous Refresh switch)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { viewModel.refreshScanner() },
                enabled = state.status != "Scanning...",
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = "Scan")
                Spacer(modifier = Modifier.width(6.dp))
                Text("Refresh")
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Auto-Refresh (5s)", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = state.isAutoRefresh,
                    onCheckedChange = { viewModel.toggleAutoRefresh(it) }
                )
            }
        }

        // Simple Tab Connection Card
        if (selectedTab == 0) {
            val rssi = state.currentRssi
            val (qualText, qualCol) = when {
                rssi >= -50 -> Pair("Excellent", ColorExcellent)
                rssi >= -67 -> Pair("Good", ColorGood)
                rssi >= -80 -> Pair("Poor", ColorPoor)
                else -> Pair("Weak/Failed", ColorFailed)
            }

            ResultCard(
                title = "Associated Wi-Fi Interface",
                statusText = qualText,
                statusColor = qualCol
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Wifi,
                        contentDescription = "Wifi Strength",
                        tint = qualCol,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(state.currentSsid, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("BSSID: ${state.currentBssid}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Signal Strength", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("$rssi dBm", fontWeight = FontWeight.Bold, color = qualCol)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Frequency Band", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val bandGhz = if (state.currentFrequencyMhz > 5000) "5.0 GHz" else "2.4 GHz"
                        Text(bandGhz, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Frequency Channel", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Ch ${state.currentChannel}", fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Negotiated Link Speed", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("${state.currentLinkSpeedMbps} Mbps", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Advanced Tab: Congestion Chart + Lists
            ResultCard(title = "Channel Utilisation Map") {
                Text(
                    text = "Congestion level per Wi-Fi spectrum channel. Tall columns represent overlapping networks (high interference risk).",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 15.sp,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Render beautiful Canvas Spectrum
                WifiChannelsChart(networks = state.nearbyNetworks)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Detected Local Access Points (${state.nearbyNetworks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            state.nearbyNetworks.forEach { router ->
                val rColor = when {
                    router.signalDbm >= -50 -> ColorExcellent
                    router.signalDbm >= -67 -> ColorGood
                    router.signalDbm >= -80 -> ColorPoor
                    else -> ColorFailed
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(router.ssid, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("${router.signalDbm} dBm", fontWeight = FontWeight.SemiBold, color = rColor, fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Channel ${router.channel}  (${if (router.frequencyMhz > 5000) "5G" else "2.4G"})", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(router.securityType, fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }

    // Permission dialog triggering
    if (showPermissionRationale) {
        PermissionRationaleDialog(
            permissionLabel = "Fine Location Scan",
            rationaleText = "To scan nearby Wi-Fi network nodes and map channel congestion levels, Android requires active location permissions. Review setting grants?",
            onDismiss = { showPermissionRationale = false },
            onConfirm = {
                locationPermissionState.launchPermissionRequest()
                showPermissionRationale = false
            }
        )
    }
}

@Composable
fun WifiChannelsChart(networks: List<WifiAnalyzerViewModel.WifiNetwork>) {
    // Group networks by 2.4G channels (ch 1 to 14) and 5G channels to build a map of channel density.
    val channelCounts = remember(networks) {
        val counts = mutableMapOf<Int, Int>()
        networks.forEach { net ->
            counts[net.channel] = (counts[net.channel] ?: 0) + 1
        }
        counts
    }

    // Common standard channels for listing we display on graph
    val targetChannels = listOf(1, 6, 11, 36, 40, 44, 48, 149, 153)
    val markerColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp)
            .border(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            val gridColor = Color.Gray.copy(alpha = 0.15f)

            // Draw horizontal dBm background grid lines
            for (level in 1..4) {
                val gridY = (height / 5) * level
                drawLine(
                    color = gridColor,
                    start = Offset(0f, gridY),
                    end = Offset(width, gridY),
                    strokeWidth = 1f
                )
            }

            // Draw bar slots for target channels
            val barCount = targetChannels.size
            val barWidth = (width / barCount) * 0.7f
            val spacing = (width / barCount) * 0.3f

            targetChannels.forEachIndexed { index, ch ->
                val density = channelCounts[ch] ?: 0
                val barHeight = ((density / 5f).coerceAtMost(1f)) * (height * 0.75f)

                val rawX = (index * (barWidth + spacing)) + (spacing / 2)
                val rawY = height - barHeight - 16f

                // Draw Bar
                val brush = Brush.verticalGradient(
                    colors = if (density > 2) listOf(ColorFailed, ColorPoor) else listOf(ColorExcellent, ColorGood)
                )

                if (density > 0) {
                    drawRect(
                        brush = brush,
                        topLeft = Offset(rawX, rawY),
                        size = Size(barWidth, barHeight)
                    )
                } else {
                    // Draw tiny placeholder block
                    drawRect(
                        color = Color.Gray.copy(alpha = 0.2f),
                        topLeft = Offset(rawX, height - 20f),
                        size = Size(barWidth, 4f)
                    )
                }

                // Draw density text above bar
                if (density > 0) {
                    // (Just simple circles since complete text drawing requires context but can draw nice indicators)
                    drawCircle(
                        color = markerColor,
                        radius = 4f,
                        center = Offset(rawX + (barWidth / 2), rawY - 8f)
                    )
                }
            }
        }

        // Horizontal channel descriptors
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            targetChannels.forEach { ch ->
                Text(
                    text = "Ch $ch",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(32.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

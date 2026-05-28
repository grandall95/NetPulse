package page.gagerandall.netpulse.ui.screens.ping

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.gagerandall.netpulse.ui.components.LatencyChart
import page.gagerandall.netpulse.ui.components.ResultCard
import page.gagerandall.netpulse.ui.theme.ColorExcellent
import page.gagerandall.netpulse.ui.theme.ColorFailed
import page.gagerandall.netpulse.ui.theme.ColorGood
import page.gagerandall.netpulse.ui.theme.ColorPoor
import java.util.Locale

@Composable
fun PingScreen(viewModel: PingViewModel) {
    val results by viewModel.pingResults.collectAsState()

    var hostInput by remember { mutableStateOf("google.com") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Simple, 1: Advanced

    // Advanced parameters
    var packetCountInput by remember { mutableStateOf("4") }
    var packetSizeInput by remember { mutableStateOf("56") }
    var ttlInput by remember { mutableStateOf("64") }
    var timeoutInput by remember { mutableStateOf("2") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ICMP Ping Analyzer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "Network Studio • Ping Utility",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Navigation tab choices (Simple vs Advanced)
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

        // Common Destination Host Input
        OutlinedTextField(
            value = hostInput,
            onValueChange = { hostInput = it },
            label = { Text("Hostname / IP Endpoint") },
            placeholder = { Text("e.g. google.com or 1.1.1.1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(18.dp),
            trailingIcon = {
                if (results.status == "Running") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Advanced Fields Panel
        if (selectedTab == 1) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Advanced Specifications",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = packetCountInput,
                            onValueChange = { packetCountInput = it },
                            label = { Text("Count") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = packetSizeInput,
                            onValueChange = { packetSizeInput = it },
                            label = { Text("Size (B)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = ttlInput,
                            onValueChange = { ttlInput = it },
                            label = { Text("TTL") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = timeoutInput,
                            onValueChange = { timeoutInput = it },
                            label = { Text("Timeout (s)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(14.dp),
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

        // Start/Stop Operation Button
        if (results.status == "Running") {
            Button(
                onClick = { viewModel.stopPing() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Stop")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Diagnostic Ping Test")
            }
        } else {
            Button(
                onClick = {
                    val cnt = packetCountInput.toIntOrNull() ?: 4
                    val sz = packetSizeInput.toIntOrNull() ?: 56
                    val timeout = timeoutInput.toIntOrNull() ?: 2
                    val ttlValue = ttlInput.toIntOrNull() ?: 64
                    
                    viewModel.startPing(
                        host = hostInput,
                        count = if (selectedTab == 0) 4 else cnt,
                        size = if (selectedTab == 0) 56 else sz,
                        timeoutSec = if (selectedTab == 0) 2 else timeout,
                        ttl = if (selectedTab == 0) 64 else ttlValue
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Diagnostic Ping Test")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Warning banner for System Fallback
        AnimatedVisibility(visible = results.isFallback) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "Warning",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Native Ping binary is restricted on this device. Running query via HTTP/Java Reachability fallback.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Live stats visual elements
        if (results.status != "Idle") {
            val average = results.avgRtt
            val (statusText, statusCol) = when {
                results.status == "Running" -> Pair("Executing...", ColorGood)
                results.status == "Failed" -> Pair("Failed", ColorFailed)
                (average <= 50f) && (results.packetLoss == 0f) -> Pair("Excellent", ColorExcellent)
                (average <= 150f) && (results.packetLoss <= 25f) -> Pair("Good", ColorGood)
                (average > 150f) || (results.packetLoss > 25f) -> Pair("Poor", ColorPoor)
                else -> Pair("Failed", ColorFailed)
            }

            ResultCard(
                title = "Diagnostic Output for $hostInput",
                statusText = statusText,
                statusColor = statusCol
            ) {
                // Key values columns
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Latency Range", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${results.minRtt.toInt()} - ${results.maxRtt.toInt()} ms",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Average Latency", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${String.format(Locale.US, "%.1f", results.avgRtt)} ms",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = statusCol
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Packet Loss Status", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${results.packetLoss.toInt()}% Loss",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (results.packetLoss > 0f) ColorFailed else ColorExcellent
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Probe Statistics", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "Sent: ${results.packetsSent} | Recv: ${results.packetsReceived}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Render Canvas Sparkline graph
                Spacer(modifier = Modifier.height(16.dp))
                Text("Dynamic Latency Trend", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                LatencyChart(dataPoints = results.individualRtts, enableZoom = selectedTab == 1)

                // Advanced detailed output list
                if (selectedTab == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Per-packet RTT Listing", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 6.dp))
                    results.individualRtts.forEachIndexed { idx, valRtt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp, horizontal = 4.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Sequence Frame #${idx + 1}", fontSize = 12.sp)
                            Text("${String.format(Locale.US, "%.1f", valRtt)} ms", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = if (valRtt < 50) ColorExcellent else if (valRtt < 150) ColorGood else ColorFailed)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Raw Binary Process Logs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Text(
                                text = if (results.rawLogs.isEmpty()) "No log frames recorded." else results.rawLogs.joinToString("\n"),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

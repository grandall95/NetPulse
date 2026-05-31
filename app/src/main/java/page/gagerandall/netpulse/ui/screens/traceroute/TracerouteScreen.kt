package page.gagerandall.netpulse.ui.screens.traceroute

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
import page.gagerandall.netpulse.ui.components.ResultCard
import page.gagerandall.netpulse.ui.theme.ColorExcellent
import page.gagerandall.netpulse.ui.theme.ColorFailed
import page.gagerandall.netpulse.ui.theme.ColorGood
import page.gagerandall.netpulse.ui.theme.ColorPoor
import java.util.Locale

@Composable
fun TracerouteScreen(viewModel: TracerouteViewModel) {
    val results by viewModel.results.collectAsState()

    var hostInput by remember { mutableStateOf("google.com") }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Simple, 1: Advanced

    // Advanced options
    var maxHopsInput by remember { mutableStateOf("30") }
    var timeoutInput by remember { mutableStateOf("1000") }
    var probesInput by remember { mutableStateOf("3") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Text(
            text = "Traceroute Scanner",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // General / Advanced Tabs
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
            value = hostInput,
            onValueChange = { hostInput = it },
            label = { Text("Target Hostname / IP") },
            placeholder = { Text("e.g. google.com or 8.8.8.8") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (results.status == "Running") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Advanced Configuration options
        if (selectedTab == 1) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Trace Threshold Specs",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = maxHopsInput,
                            onValueChange = { maxHopsInput = it },
                            label = { Text("Max Hops") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 4.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = probesInput,
                            onValueChange = { probesInput = it },
                            label = { Text("Probes") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp),
                            singleLine = true
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = timeoutInput,
                        onValueChange = { timeoutInput = it },
                        label = { Text("Timeout per hop (ms)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (results.status == "Running") {
            Button(
                onClick = { viewModel.stopTraceroute() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Stop")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Route Trace Scanner")
            }
        } else {
            Button(
                onClick = {
                    val mh = maxHopsInput.toIntOrNull() ?: 30
                    val pr = probesInput.toIntOrNull() ?: 3
                    val tms = timeoutInput.toIntOrNull() ?: 1000

                    viewModel.startTraceroute(
                        host = hostInput,
                        maxHops = if (selectedTab == 0) 30 else mh,
                        timeoutMs = if (selectedTab == 0) 1000 else tms
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Route Trace Scanner")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Fallback simulation indicator
        AnimatedVisibility(visible = results.isFallback) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Info",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Note: Standard socket traceroute raw probes require root. Rendering hops from a verified lookup index.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }

        // Render Tracing Results
        if (results.status != "Idle") {
            ResultCard(
                title = "Traceroute Results: $hostInput",
                statusText = if (results.status == "Running") "Running" else if (results.routeComplete) "Complete" else "Incomplete",
                statusColor = if (results.status == "Running") ColorGood else if (results.routeComplete) ColorExcellent else ColorPoor
            ) {
                if (results.hops.isEmpty()) {
                    Text("Tracing network hops, please stay connected...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                // Node timeline representation
                results.hops.forEach { hop ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = hop.hopNumber.toString(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = hop.hostname,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = hop.ipAddress,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = "${String.format(Locale.US, "%.1f", hop.rttMs)} ms",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (hop.rttMs < 40f) ColorExcellent else if (hop.rttMs < 110f) ColorGood else ColorPoor
                        )
                    }
                }

                if (results.status == "Complete") {
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (results.routeComplete) ColorExcellent.copy(alpha = 0.15f) else ColorFailed.copy(alpha = 0.15f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (results.routeComplete) "🚀 Route completed successfully. Destination reached." else "⚠️ Route terminated early. Destination unreachable or blocked.",
                            fontWeight = FontWeight.SemiBold,
                            color = if (results.routeComplete) ColorExcellent else ColorFailed,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (selectedTab == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Raw Output Packets", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text(
                                text = if (results.rawOutput.isEmpty()) "Initiating streams..." else results.rawOutput.joinToString("\n"),
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

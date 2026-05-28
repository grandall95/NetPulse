package page.gagerandall.netpulse.ui.screens.latency

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.gagerandall.netpulse.ui.components.LatencyChart
import page.gagerandall.netpulse.ui.components.ResultCard
import page.gagerandall.netpulse.ui.theme.ColorExcellent
import page.gagerandall.netpulse.ui.theme.ColorFailed
import page.gagerandall.netpulse.ui.theme.ColorGood
import page.gagerandall.netpulse.ui.theme.ColorPoor

@Composable
fun LatencyGraphScreen(viewModel: LatencyGraphViewModel) {
    val state by viewModel.state.collectAsState()
    var hostInput by remember { mutableStateOf("8.8.8.8") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Live Latency Tracker",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = hostInput,
            onValueChange = { hostInput = it },
            label = { Text("Traced IP / Domain Node") },
            placeholder = { Text("e.g. 8.8.8.8") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !state.activeTracking
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Activation Button
        Button(
            onClick = { viewModel.toggleTracking(hostInput, !state.activeTracking) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (state.activeTracking) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(
                imageVector = if (state.activeTracking) Icons.Default.Stop else Icons.Default.PlayArrow,
                contentDescription = "Control"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (state.activeTracking) "Stop Tracking" else "Start Tracking")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quality graph presentation card
        if (state.historyPoints.isNotEmpty() || state.activeTracking) {
            val curr = state.currentMs
            val (lbl, col) = when {
                curr == 0f -> Pair("Probing core...", Color.Gray)
                curr < 50f -> Pair("Excellent Connection Quality", ColorExcellent)
                curr < 150f -> Pair("Stable / Intermittent Latency", ColorGood)
                curr >= 150f -> Pair("Congested / Poor Latency", ColorPoor)
                else -> Pair("Timeout / Dropped Frame", ColorFailed)
            }

            ResultCard(
                title = "Live Stream: ${state.ipName}",
                statusText = lbl,
                statusColor = col
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Current Ping Response Duration", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = "${curr.toInt()} ms",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = col
                        )
                    }
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Probes sent", fontSize = 10.sp)
                            Text(state.packetsScanned.toString(), fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Historical Auto-Scrolling Sparkline (Current Frame: ${state.historyPoints.size}/40)",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Large embeddable LatencyChart canvas component
                LatencyChart(
                    dataPoints = state.historyPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    enableZoom = true
                )
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No Active Tracking", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("Activate live track queries of selected hostname endpoints above.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

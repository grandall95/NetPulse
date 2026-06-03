package page.gagerandall.netpulse.ui.screens.whois

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.gagerandall.netpulse.ui.components.ResultCard
import page.gagerandall.netpulse.ui.theme.ColorExcellent
import page.gagerandall.netpulse.ui.theme.ColorGood
import page.gagerandall.netpulse.LocalTvMode
import page.gagerandall.netpulse.util.tvFocusable
import androidx.compose.ui.input.key.*
import kotlinx.coroutines.launch

@Composable
fun WhoisScreen(viewModel: WhoisViewModel) {
    val isTv = LocalTvMode.current
    val state by viewModel.state.collectAsState()
    var domainInput by remember { mutableStateOf("google.com") }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Simple, 1: Advanced

    val scrollState = rememberScrollState()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Text(
            text = "WHOIS Registry Query",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        SecondaryTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
            value = domainInput,
            onValueChange = { domainInput = it },
            label = { Text("Domain Name / IP Endpoint") },
            placeholder = { Text("e.g. google.com or 8.8.8.8") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            readOnly = state.status == "Running",
            trailingIcon = {
                if (state.status == "Running") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        val isRunning = state.status == "Running"
        Button(
            onClick = {
                if (isRunning) {
                    viewModel.stopWhois()
                } else {
                    viewModel.queryWhois(domainInput)
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isRunning) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                contentColor = if (isRunning) MaterialTheme.colorScheme.onError else MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = if (isRunning) Icons.Default.Close else Icons.Default.PlayArrow,
                contentDescription = if (isRunning) "Stop" else "Run"
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (isRunning) "Stop Registrar Audit" else "Launch Registrar Audit")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (state.status == "Failed") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Lookup Failed: ${state.errorMessage}",
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        // Render Results Panel
        if ((state.status != "Idle") && (state.status != "Failed")) {
            ResultCard(
                title = "Registry records for $domainInput",
                statusText = if (state.status == "Running") "Querying..." else "Fetched",
                statusColor = if (state.status == "Running") ColorGood else ColorExcellent
            ) {
                // Key registration grids
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(isTv, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Authoritative Registrar", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.registrar, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Registrant Org", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.registrantOrg, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(isTv, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Creation Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.creationDate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Expiry Date", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(state.expiryDate, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                Spacer(modifier = Modifier.height(12.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .tvFocusable(isTv, RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    Text("Name Servers Configured", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    if (state.nameServers.isEmpty()) {
                        Text("No explicit nameservers returned in primary fields.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        state.nameServers.forEach { ns ->
                            Text(
                                text = "• $ns",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(vertical = 2.dp)
                            )
                        }
                    }
                }

                // Advanced monospace output card
                if (selectedTab == 1 && state.status == "Complete") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Full Registry Response", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    val rawScrollState = rememberScrollState()
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 250.dp)
                            .tvFocusable(isTv, RoundedCornerShape(8.dp))
                            .verticalScroll(rawScrollState)
                            .onPreviewKeyEvent { keyEvent ->
                                if (isTv && keyEvent.type == KeyEventType.KeyDown) {
                                    when (keyEvent.key) {
                                        Key.DirectionUp -> {
                                            if (rawScrollState.value > 0) {
                                                coroutineScope.launch {
                                                    rawScrollState.animateScrollTo((rawScrollState.value - 50).coerceAtLeast(0))
                                                }
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        Key.DirectionDown -> {
                                            if (rawScrollState.value < rawScrollState.maxValue) {
                                                coroutineScope.launch {
                                                    rawScrollState.animateScrollTo((rawScrollState.value + 50).coerceAtMost(rawScrollState.maxValue))
                                                }
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        else -> false
                                    }
                                } else {
                                    false
                                }
                            }
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

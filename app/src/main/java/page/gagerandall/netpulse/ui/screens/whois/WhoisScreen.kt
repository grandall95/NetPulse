package page.gagerandall.netpulse.ui.screens.whois

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.gagerandall.netpulse.ui.components.ResultCard
import page.gagerandall.netpulse.ui.theme.ColorExcellent
import page.gagerandall.netpulse.ui.theme.ColorGood

@Composable
fun WhoisScreen(viewModel: WhoisViewModel) {
    val state by viewModel.state.collectAsState()
    var domainInput by remember { mutableStateOf("google.com") }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Simple, 1: Advanced

    val scrollState = rememberScrollState()

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
            value = domainInput,
            onValueChange = { domainInput = it },
            label = { Text("Domain Name / IP Endpoint") },
            placeholder = { Text("e.g. google.com or 8.8.8.8") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (state.status == "Running") {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.status == "Running") {
            Button(
                onClick = { viewModel.stopWhois() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Close, contentDescription = "Stop")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Stop Registrar Audit")
            }
        } else {
            Button(
                onClick = { viewModel.queryWhois(domainInput) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Run")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Launch Registrar Audit")
            }
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
                Row(modifier = Modifier.fillMaxWidth()) {
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

                Row(modifier = Modifier.fillMaxWidth()) {
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

                // Advanced monospace output card
                if (selectedTab == 1 && state.status == "Complete") {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Full Registry Response", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            modifier = Modifier
                                .padding(8.dp)
                                .heightIn(max = 250.dp)
                                .verticalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}

package page.gagerandall.netpulse.ui.screens.subnet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.gagerandall.netpulse.ui.components.ResultCard
import page.gagerandall.netpulse.ui.theme.ColorExcellent
import page.gagerandall.netpulse.ui.theme.ColorGood

@Composable
fun SubnetCalculatorScreen(viewModel: SubnetCalculatorViewModel) {
    val state by viewModel.state.collectAsState()

    var ipInput by remember { mutableStateOf("192.168.1.1") }
    // Slider state
    var cidrInput by remember { mutableFloatStateOf(24f) }
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Simple, 1: Advanced

    // Splits state slider
    var splitCountInput by remember { mutableFloatStateOf(4f) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Text(
            text = "IP Subnet Calculator",
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
            value = ipInput,
            onValueChange = {
                ipInput = it
                viewModel.recalculateSubnet(it, cidrInput.toInt(), splitCountInput.toInt())
            },
            label = { Text("Source IP Address") },
            placeholder = { Text("e.g. 192.168.1.1 or 2001:db8::1") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Prefix slider
        val isV6 = ipInput.trim().contains(":")
        val maxCidr = if (isV6) 128f else 32f

        Text(
            text = "CIDR Prefix: /${cidrInput.toInt()}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        Slider(
            value = cidrInput,
            onValueChange = {
                cidrInput = it
                viewModel.recalculateSubnet(ipInput, it.toInt(), splitCountInput.toInt())
            },
            valueRange = 0f..maxCidr,
            steps = maxCidr.toInt() - 1
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Error message card
        if (state.errorMessage != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text(text = state.errorMessage ?: "", color = MaterialTheme.colorScheme.onErrorContainer, modifier = Modifier.padding(12.dp))
            }
        }

        // Calculation Results Group
        state.details?.let { details ->
            ResultCard(
                title = "CIDR Broadcast / Subnet details",
                statusText = if (details.isIpv6) "IPv6 Address" else "IPv4 Address",
                statusColor = if (details.isIpv6) ColorGood else ColorExcellent
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Network Base Address", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(details.networkAddress, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                    }
                    if (!details.isIpv6) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Broadcast Address", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(details.broadcastAddress, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("First Usable Host", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(details.firstUsable, fontWeight = FontWeight.Bold)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Last Usable Host", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(details.lastUsable, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Subnet Mask IP", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(details.subnetMask, fontWeight = FontWeight.Bold)
                    }
                    if (!details.isIpv6) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Wildcard Mask", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(details.wildcardMask, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Column {
                    Text("Total Usable Hosts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(
                        text = if (details.isIpv6) "3.4 × 10³⁸ total spaces" else details.totalUsable,
                        fontWeight = FontWeight.ExtraBold,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Advanced Tab: Subnet splitting allocations
                if (selectedTab == 1) {
                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text("Subnet Splitting", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(6.dp))

                    if (details.isIpv6) {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.Info, contentDescription = "Info")
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("IPv6 prefix allocations split with standard hexadecimal increments.", fontSize = 11.sp)
                            }
                        }
                    } else {
                        // Splits slider
                        Text("Allocated Subnets Count: ${splitCountInput.toInt()}", fontSize = 11.sp)
                        Slider(
                            value = splitCountInput,
                            onValueChange = {
                                splitCountInput = it
                                viewModel.recalculateSubnet(ipInput, cidrInput.toInt(), it.toInt())
                            },
                            valueRange = 2f..16f,
                            steps = 6
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        state.splitSubnets.forEachIndexed { num, split ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Allocated Block #${num + 1}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("${split.first} /${split.second}", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

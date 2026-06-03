package page.gagerandall.netpulse.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import page.gagerandall.netpulse.BuildConfig
import page.gagerandall.netpulse.LocalTvMode
import page.gagerandall.netpulse.util.tvFocusable

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val theme by viewModel.themeState.collectAsState()

    var showAboutDialog by remember { mutableStateOf(false) }

    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()
    val isTv = LocalTvMode.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
    ) {
        Text(
            text = "Settings & Preferences",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // Theme Toggle Section
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ColorLens,
                        contentDescription = "Theme Icon",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 12.dp)
                    )
                    Text(
                        text = "Theme Selection",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    listOf("System", "Light", "Dark").forEach { option ->
                        val isSelected = theme == option
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.setTheme(option) },
                            label = { Text(text = option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .tvFocusable(isTv, RoundedCornerShape(8.dp), focusable = false)
                        )
                    }
                }
            }
        }



        // About / Open Source Licenses
        // - Resolved a double-focus / double-click bug on the settings screen by introducing a `focusable: Boolean = true` parameter to our custom `tvFocusable` modifier. For components that are already clickable or focusable (such as `FilterChip` and `Card` layouts with `.clickable`), passing `focusable = false` avoids nesting multiple focus nodes while keeping the premium hover border/scaling design effect.
        // - Enabled remote-friendly D-pad scrolling inside the Open Source Credits dialog by wrapping individual library items inside focusable `Column` blocks (using `.tvFocusable(isTv, ...)`), allowing the remote focus hierarchy to smoothly bring intermediate items into view while navigating down.
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
                .clickable { showAboutDialog = true }
                .tvFocusable(isTv, RoundedCornerShape(16.dp), focusable = false),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(end = 12.dp)
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "About & Licenses",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "NetPulse v${BuildConfig.VERSION_NAME}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = "View Info")
            }
        }
    }


    // Open Source Libraries Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "Open Source Integration Credits",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .height(350.dp)
                        .verticalScroll(rememberScrollState()),
                ) {
                    Text(
                        text = "This toolkit is compiled using the following open-source libraries:",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // DnsJava
                    Column(modifier = Modifier.fillMaxWidth().tvFocusable(isTv, RoundedCornerShape(6.dp)).padding(4.dp)) {
                        Text("DnsJava (Version 3.6.5)", fontWeight = FontWeight.Bold)
                        Text("High performance, custom DNS packet resolver & parser.\nLicense: BSD 3-Clause", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // OkHttp
                    Column(modifier = Modifier.fillMaxWidth().tvFocusable(isTv, RoundedCornerShape(6.dp)).padding(4.dp)) {
                        Text("Square OkHttp (Version 4.12.0)", fontWeight = FontWeight.Bold)
                        Text("Advanced connection pooling, header inspection, and automated TLS/SSL client APIs.\nLicense: Apache 2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Accompanist Permissions
                    Column(modifier = Modifier.fillMaxWidth().tvFocusable(isTv, RoundedCornerShape(6.dp)).padding(4.dp)) {
                        Text("Google Accompanist Permissions (Version 0.37.0)", fontWeight = FontWeight.Bold)
                        Text("Idiomatic Jetpack Compose runtime permission workflows.\nLicense: Apache 2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Datastore
                    Column(modifier = Modifier.fillMaxWidth().tvFocusable(isTv, RoundedCornerShape(6.dp)).padding(4.dp)) {
                        Text("Androidx Jetpack DataStore (Version 1.1.3)", fontWeight = FontWeight.Bold)
                        Text("Type-safe, persistent local preferences backed by Kotlin coroutines.\nLicense: Apache 2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    // Jetpack Compose
                    Column(modifier = Modifier.fillMaxWidth().tvFocusable(isTv, RoundedCornerShape(6.dp)).padding(4.dp)) {
                        Text("Jetpack Compose Material 3", fontWeight = FontWeight.Bold)
                        Text("Declarative, interactive UI layouts built with Google's M3 specifications.\nLicense: Apache 2.0", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Developed by Gage Randall",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://www.gagerandall.page/contact") }
                            .tvFocusable(isTv, RoundedCornerShape(4.dp), focusable = false)
                            .padding(vertical = 4.dp)
                    )

                    Text(
                        text = "Built with Open Source in Mind: GNU General Public License version 3",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .clickable { uriHandler.openUri("https://opensource.org/license/GPL-3.0") }
                            .tvFocusable(isTv, RoundedCornerShape(4.dp), focusable = false)
                            .padding(vertical = 4.dp)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}

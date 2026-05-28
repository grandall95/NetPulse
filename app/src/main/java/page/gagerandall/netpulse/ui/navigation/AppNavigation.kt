package page.gagerandall.netpulse.ui.navigation

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.automirrored.outlined.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import page.gagerandall.netpulse.ui.screens.dns.DnsLookupScreen
import page.gagerandall.netpulse.ui.screens.dns.DnsLookupViewModel
import page.gagerandall.netpulse.ui.screens.whois.WhoisScreen
import page.gagerandall.netpulse.ui.screens.whois.WhoisViewModel
import androidx.compose.ui.unit.sp
import page.gagerandall.netpulse.ui.screens.header.HttpHeaderScreen
import page.gagerandall.netpulse.ui.screens.header.HttpHeaderViewModel
import page.gagerandall.netpulse.ui.screens.ipinfo.IpInfoScreen
import page.gagerandall.netpulse.ui.screens.ipinfo.IpInfoViewModel
import page.gagerandall.netpulse.ui.screens.latency.LatencyGraphScreen
import page.gagerandall.netpulse.ui.screens.latency.LatencyGraphViewModel
import page.gagerandall.netpulse.ui.screens.ping.PingScreen
import page.gagerandall.netpulse.ui.screens.ping.PingViewModel
import page.gagerandall.netpulse.ui.screens.portscan.PortScannerScreen
import page.gagerandall.netpulse.ui.screens.portscan.PortScannerViewModel
import page.gagerandall.netpulse.ui.screens.settings.SettingsScreen
import page.gagerandall.netpulse.ui.screens.settings.SettingsViewModel
import page.gagerandall.netpulse.ui.screens.subnet.SubnetCalculatorScreen
import page.gagerandall.netpulse.ui.screens.subnet.SubnetCalculatorViewModel
import page.gagerandall.netpulse.ui.screens.traceroute.TracerouteScreen
import page.gagerandall.netpulse.ui.screens.traceroute.TracerouteViewModel
import page.gagerandall.netpulse.ui.screens.wifi.WifiAnalyzerScreen
import page.gagerandall.netpulse.ui.screens.wifi.WifiAnalyzerViewModel
import page.gagerandall.netpulse.ui.screens.speedtest.SpeedTestScreen
import page.gagerandall.netpulse.ui.screens.speedtest.SpeedTestViewModel
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight

enum class NavigationRoutes(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    PING("ping", "Ping", Icons.Default.NetworkPing, Icons.Outlined.NetworkPing),
    TRACEROUTE("traceroute", "Trace", Icons.Default.Route, Icons.Outlined.Route),
    SPEEDTEST("speedtest", "Speed", Icons.Default.Speed, Icons.Outlined.Speed),
    DNS("dns", "DNS", Icons.Default.Dns, Icons.Outlined.Dns),
    PORTSCAN("portscan", "Ports", Icons.Default.GridOn, Icons.Outlined.GridOn),
    WHOIS("whois", "Whois", Icons.AutoMirrored.Filled.Assignment, Icons.AutoMirrored.Outlined.Assignment),
    WIFI("wifi", "Wi-Fi", Icons.Default.Wifi, Icons.Outlined.Wifi),
    IPINFO("ipinfo", "IP Info", Icons.Default.Public, Icons.Outlined.Public),
    HEADER("header", "Headers", Icons.Default.Http, Icons.Outlined.Http),
    LATENCY("latency", "Quality", Icons.Default.Timeline, Icons.Outlined.Timeline),
    SUBNET("subnet", "Subnet", Icons.Default.Calculate, Icons.Outlined.Calculate),
    SETTINGS("settings", "Prefs", Icons.Default.Settings, Icons.Outlined.Settings)
}

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    pingViewModel: PingViewModel,
    tracerouteViewModel: TracerouteViewModel,
    speedTestViewModel: SpeedTestViewModel,
    dnsLookupViewModel: DnsLookupViewModel,
    portScannerViewModel: PortScannerViewModel,
    whoisViewModel: WhoisViewModel,
    wifiAnalyzerViewModel: WifiAnalyzerViewModel,
    ipInfoViewModel: IpInfoViewModel,
    httpHeaderViewModel: HttpHeaderViewModel,
    latencyGraphViewModel: LatencyGraphViewModel,
    subnetCalculatorViewModel: SubnetCalculatorViewModel,
    settingsViewModel: SettingsViewModel,
    isLargeScreen: Boolean = false // Passed in dynamically via check class
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationRoutes.PING.route

    Row(modifier = Modifier.fillMaxSize()) {
        // Render Side elements if on wider screen layouts, styled to match the Bento visual theme and scrollable
        if (isLargeScreen) {
            Card(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(76.dp)
                    .padding(start = 12.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            ) {
                ScrollableNavigationColumn(
                    currentRoute = currentRoute,
                    items = NavigationRoutes.values(),
                    onNavigate = { dest ->
                        if (currentRoute != dest.route) {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }

        // Host screen content
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = NavigationRoutes.PING.route,
                    modifier = Modifier.fillMaxSize()
                ) {
                    composable(NavigationRoutes.PING.route) {
                        PingScreen(viewModel = pingViewModel)
                    }
                    composable(NavigationRoutes.TRACEROUTE.route) {
                        TracerouteScreen(viewModel = tracerouteViewModel)
                    }
                    composable(NavigationRoutes.SPEEDTEST.route) {
                        SpeedTestScreen(viewModel = speedTestViewModel)
                    }
                    composable(NavigationRoutes.DNS.route) {
                        DnsLookupScreen(viewModel = dnsLookupViewModel)
                    }
                    composable(NavigationRoutes.PORTSCAN.route) {
                        PortScannerScreen(viewModel = portScannerViewModel)
                    }
                    composable(NavigationRoutes.WHOIS.route) {
                        WhoisScreen(viewModel = whoisViewModel)
                    }
                    composable(NavigationRoutes.WIFI.route) {
                        WifiAnalyzerScreen(viewModel = wifiAnalyzerViewModel)
                    }
                    composable(NavigationRoutes.IPINFO.route) {
                        IpInfoScreen(viewModel = ipInfoViewModel)
                    }
                    composable(NavigationRoutes.HEADER.route) {
                        HttpHeaderScreen(viewModel = httpHeaderViewModel)
                    }
                    composable(NavigationRoutes.LATENCY.route) {
                        LatencyGraphScreen(viewModel = latencyGraphViewModel)
                    }
                    composable(NavigationRoutes.SUBNET.route) {
                        SubnetCalculatorScreen(viewModel = subnetCalculatorViewModel)
                    }
                    composable(NavigationRoutes.SETTINGS.route) {
                        SettingsScreen(viewModel = settingsViewModel)
                    }
                }
            }

            // Render bottom control panel on standard vertical phone structures
            if (!isLargeScreen) {
                // Since M3 bottom navigation can look cramped with 11 items, we use a beautifully scrollable row
                // that lets the user swipe side-to-side through all diagnostic tools in a floating Bento pill!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 4.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    val list = NavigationRoutes.values()
                    ScrollableNavigationRow(
                        currentRoute = currentRoute,
                        items = list,
                        onNavigate = { dest ->
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ScrollableNavigationRow(
    currentRoute: String,
    items: Array<NavigationRoutes>,
    onNavigate: (NavigationRoutes) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .horizontalScroll(scrollState),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        items.forEach { dest ->
            val isSelected = currentRoute == dest.route
            Column(
                modifier = Modifier
                    .width(66.dp)
                    .fillMaxHeight()
                    .clickable { onNavigate(dest) },
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp, 28.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dest.title,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ScrollableNavigationColumn(
    currentRoute: String,
    items: Array<NavigationRoutes>,
    onNavigate: (NavigationRoutes) -> Unit
) {
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp, androidx.compose.ui.Alignment.Top)
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        items.forEach { dest ->
            val isSelected = currentRoute == dest.route
            Column(
                modifier = Modifier
                    .width(60.dp)
                    .clickable { onNavigate(dest) },
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp, 28.dp)
                        .background(
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else androidx.compose.ui.graphics.Color.Transparent,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                        contentDescription = dest.title,
                        tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = dest.title,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

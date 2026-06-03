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
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay

import page.gagerandall.netpulse.LocalTvMode
import androidx.tv.material3.DrawerValue
import androidx.tv.material3.ModalNavigationDrawer
import androidx.tv.material3.NavigationDrawerItem
import androidx.tv.material3.rememberDrawerState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.runtime.LaunchedEffect

/**
 * Defines all available routes in the application with associated metadata.
 * Includes title and icons for both selected and unselected states.
 */
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
    SETTINGS("settings", "Prefs", Icons.Default.Settings, Icons.Outlined.Settings),
}

/**
 * Primary navigation component that handles responsive layout (Phone, Tablet, TV).
 */
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    settingsViewModel: SettingsViewModel,
    isLargeScreen: Boolean = false, // Passed in dynamically via check class
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: NavigationRoutes.PING.route

    val isTv = LocalTvMode.current

    if (isTv) {
        // Android TV Specific Layout: Side Drawer with D-pad focus management
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val focusRequesters = remember { NavigationRoutes.entries.associateWith { FocusRequester() } }
        val lazyListState = rememberLazyListState()

        // Handle automatic focus on the active menu item when the drawer opens
        LaunchedEffect(drawerState.currentValue) {
            if (drawerState.currentValue == DrawerValue.Open) {
                val currentRouteIndex = NavigationRoutes.entries.indexOfFirst { it.route == currentRoute }
                if (currentRouteIndex != -1) {
                    lazyListState.scrollToItem(currentRouteIndex)
                    delay(80.milliseconds)
                    NavigationRoutes.entries.getOrNull(currentRouteIndex)?.let { currentRouteEnum ->
                        focusRequesters[currentRouteEnum]?.requestFocus()
                    }
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = { drawerValue ->
                val isClosed = drawerValue == DrawerValue.Closed
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.Start
                ) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isClosed) "NP" else "NetPulse TV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 12.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    itemsIndexed(NavigationRoutes.entries.toTypedArray()) { _, dest ->
                        val isSelected = currentRoute == dest.route
                        val onItemClick = remember(dest.route) {
                            {
                                if (currentRoute != dest.route) {
                                    navController.navigate(dest.route) {
                                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            }
                        }
                        
                        NavigationDrawerItem(
                            selected = isSelected,
                            onClick = onItemClick,
                            modifier = Modifier.focusRequester(focusRequesters[dest] ?: remember { FocusRequester() }),
                            leadingContent = {
                                Icon(
                                    imageVector = if (isSelected) dest.selectedIcon else dest.unselectedIcon,
                                    contentDescription = dest.title,
                                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            content = {
                                if (!isClosed) {
                                    Text(
                                        text = dest.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        )
                    }
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 80.dp, end = 16.dp),
            ) {
                AppNavHost(
                    navController = navController,
                    settingsViewModel = settingsViewModel,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    } else {
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
                        items = NavigationRoutes.entries.toTypedArray(),
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
                    .fillMaxHeight(),
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    AppNavHost(
                        navController = navController,
                        settingsViewModel = settingsViewModel,
                        modifier = Modifier.fillMaxSize(),
                    )
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
                        val list = NavigationRoutes.entries.toTypedArray()
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

/**
 * Defines the navigation graph.
 * ViewModels are initialized lazily via the `viewModel()` delegate to optimize startup speed.
 */
@Composable
fun AppNavHost(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = NavigationRoutes.PING.route,
        modifier = modifier,
    ) {
        composable(NavigationRoutes.PING.route) {
            val viewModel: PingViewModel = viewModel()
            PingScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.TRACEROUTE.route) {
            val viewModel: TracerouteViewModel = viewModel()
            TracerouteScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.SPEEDTEST.route) {
            val viewModel: SpeedTestViewModel = viewModel()
            SpeedTestScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.DNS.route) {
            val viewModel: DnsLookupViewModel = viewModel()
            DnsLookupScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.PORTSCAN.route) {
            val viewModel: PortScannerViewModel = viewModel()
            PortScannerScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.WHOIS.route) {
            val viewModel: WhoisViewModel = viewModel()
            WhoisScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.WIFI.route) {
            val viewModel: WifiAnalyzerViewModel = viewModel()
            WifiAnalyzerScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.IPINFO.route) {
            val viewModel: IpInfoViewModel = viewModel()
            IpInfoScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.HEADER.route) {
            val viewModel: HttpHeaderViewModel = viewModel()
            HttpHeaderScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.LATENCY.route) {
            val viewModel: LatencyGraphViewModel = viewModel()
            LatencyGraphScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.SUBNET.route) {
            val viewModel: SubnetCalculatorViewModel = viewModel()
            SubnetCalculatorScreen(viewModel = viewModel)
        }
        composable(NavigationRoutes.SETTINGS.route) {
            SettingsScreen(viewModel = settingsViewModel)
        }
    }
}

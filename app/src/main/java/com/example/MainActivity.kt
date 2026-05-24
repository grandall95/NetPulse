package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import com.example.core.SettingsStore
import com.example.ui.navigation.AppNavigation
import com.example.ui.screens.dns.DnsLookupViewModel
import com.example.ui.screens.header.HttpHeaderViewModel
import com.example.ui.screens.ipinfo.IpInfoViewModel
import com.example.ui.screens.latency.LatencyGraphViewModel
import com.example.ui.screens.ping.PingViewModel
import com.example.ui.screens.portscan.PortScannerViewModel
import com.example.ui.screens.settings.SettingsViewModel
import com.example.ui.screens.speedtest.SpeedTestViewModel
import com.example.ui.screens.subnet.SubnetCalculatorViewModel
import com.example.ui.screens.traceroute.TracerouteViewModel
import com.example.ui.screens.whois.WhoisViewModel
import com.example.ui.screens.wifi.WifiAnalyzerViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize state stores
        val settingsStore = SettingsStore(applicationContext)

        // Initialize ViewModels
        val pingViewModel = PingViewModel()
        val tracerouteViewModel = TracerouteViewModel()
        val speedTestViewModel = SpeedTestViewModel()
        val dnsLookupViewModel = DnsLookupViewModel()
        val portScannerViewModel = PortScannerViewModel()
        val whoisViewModel = WhoisViewModel()
        val wifiAnalyzerViewModel = WifiAnalyzerViewModel(application)
        val ipInfoViewModel = IpInfoViewModel()
        val httpHeaderViewModel = HttpHeaderViewModel()
        val latencyGraphViewModel = LatencyGraphViewModel()
        val subnetCalculatorViewModel = SubnetCalculatorViewModel()
        val settingsViewModel = SettingsViewModel(application)

        setContent {
            val themeState by settingsViewModel.themeState.collectAsState()
            val useDarkTheme = when (themeState) {
                "Dark" -> true
                "Light" -> false
                else -> true // default dark cosmic profile
            }

            MyApplicationTheme(darkTheme = useDarkTheme) {
                val config = LocalConfiguration.current
                val isLargeScreen = config.screenWidthDp >= 600

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        AppNavigation(
                            pingViewModel = pingViewModel,
                            tracerouteViewModel = tracerouteViewModel,
                            speedTestViewModel = speedTestViewModel,
                            dnsLookupViewModel = dnsLookupViewModel,
                            portScannerViewModel = portScannerViewModel,
                            whoisViewModel = whoisViewModel,
                            wifiAnalyzerViewModel = wifiAnalyzerViewModel,
                            ipInfoViewModel = ipInfoViewModel,
                            httpHeaderViewModel = httpHeaderViewModel,
                            latencyGraphViewModel = latencyGraphViewModel,
                            subnetCalculatorViewModel = subnetCalculatorViewModel,
                            settingsViewModel = settingsViewModel,
                            isLargeScreen = isLargeScreen
                        )
                    }
                }
            }
        }
    }
}

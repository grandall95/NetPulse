package page.gagerandall.netpulse

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
import page.gagerandall.netpulse.core.SettingsStore
import page.gagerandall.netpulse.ui.navigation.AppNavigation
import page.gagerandall.netpulse.ui.screens.dns.DnsLookupViewModel
import page.gagerandall.netpulse.ui.screens.header.HttpHeaderViewModel
import page.gagerandall.netpulse.ui.screens.ipinfo.IpInfoViewModel
import page.gagerandall.netpulse.ui.screens.latency.LatencyGraphViewModel
import page.gagerandall.netpulse.ui.screens.ping.PingViewModel
import page.gagerandall.netpulse.ui.screens.portscan.PortScannerViewModel
import page.gagerandall.netpulse.ui.screens.settings.SettingsViewModel
import page.gagerandall.netpulse.ui.screens.speedtest.SpeedTestViewModel
import page.gagerandall.netpulse.ui.screens.subnet.SubnetCalculatorViewModel
import page.gagerandall.netpulse.ui.screens.traceroute.TracerouteViewModel
import page.gagerandall.netpulse.ui.screens.whois.WhoisViewModel
import page.gagerandall.netpulse.ui.screens.wifi.WifiAnalyzerViewModel
import page.gagerandall.netpulse.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Initialize state stores
        SettingsStore(applicationContext)

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

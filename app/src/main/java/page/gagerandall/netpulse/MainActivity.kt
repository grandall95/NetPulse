package page.gagerandall.netpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import android.app.UiModeManager
import android.content.res.Configuration

val LocalTvMode = staticCompositionLocalOf { false }

/**
 * Main entry point for the application.
 * Handles edge-to-edge display, TV mode detection, and root UI composition.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable edge-to-edge support for modern Android layouts
        enableEdgeToEdge()

        // Detect if the app is running on an Android TV device
        val uiModeManager = getSystemService(UI_MODE_SERVICE) as? UiModeManager
        val isTv = uiModeManager?.currentModeType == Configuration.UI_MODE_TYPE_TELEVISION

        // Initialize state stores
        SettingsStore(applicationContext)

        // Initialize SettingsViewModel (required for theme state at the root level)
        val settingsViewModel = SettingsViewModel(application)

        setContent {
            // Observe theme selection from DataStore
            val themeState by settingsViewModel.themeState.collectAsState()
            val useDarkTheme = when (themeState) {
                "Dark" -> true
                "Light" -> false
                else -> isSystemInDarkTheme()
            }

            // Provide TV mode status to the entire UI tree via CompositionLocal
            CompositionLocalProvider(LocalTvMode provides isTv) {
                MyApplicationTheme(darkTheme = useDarkTheme) {
                    val config = LocalConfiguration.current
                    // Determine if the screen is large enough for a side navigation panel (>= 600dp)
                    val isLargeScreen = config.screenWidthDp >= 600

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            // Main navigation host
                            AppNavigation(
                                settingsViewModel = settingsViewModel,
                                isLargeScreen = isLargeScreen,
                            )
                        }
                    }
                }
            }
        }
    }
}

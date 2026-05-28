package page.gagerandall.netpulse.ui.screens.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import page.gagerandall.netpulse.core.SettingsStore
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsStore = SettingsStore(application)

    val themeState: StateFlow<String> = settingsStore.themeFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "System")

    val dnsState: StateFlow<String> = settingsStore.dnsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    val portConcurrencyState: StateFlow<Int> = settingsStore.portConcurrencyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50)

    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsStore.setTheme(theme)
        }
    }

    fun setDefaultDns(dns: String) {
        viewModelScope.launch {
            settingsStore.setDefaultDns(dns)
        }
    }

    fun setPortConcurrency(limit: Int) {
        viewModelScope.launch {
            settingsStore.setPortConcurrency(limit)
        }
    }
}

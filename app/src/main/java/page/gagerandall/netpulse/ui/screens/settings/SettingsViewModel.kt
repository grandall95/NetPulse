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


    fun setTheme(theme: String) {
        viewModelScope.launch {
            settingsStore.setTheme(theme)
        }
    }


}

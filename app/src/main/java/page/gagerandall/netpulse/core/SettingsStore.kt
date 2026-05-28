package page.gagerandall.netpulse.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netpulse_settings")

class SettingsStore(private val context: Context) {

    companion object {
        val THEME_KEY = stringPreferencesKey("theme")
        val DNS_KEY = stringPreferencesKey("default_dns")
        val PORT_CONCURRENCY_KEY = intPreferencesKey("port_concurrency")
    }

    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "System"
    }

    val dnsFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[DNS_KEY] ?: "system"
    }

    val portConcurrencyFlow: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[PORT_CONCURRENCY_KEY] ?: 50
    }

    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }

    suspend fun setDefaultDns(dns: String) {
        context.dataStore.edit { prefs ->
            prefs[DNS_KEY] = dns
        }
    }

    suspend fun setPortConcurrency(limit: Int) {
        context.dataStore.edit { prefs ->
            prefs[PORT_CONCURRENCY_KEY] = limit
        }
    }
}

package page.gagerandall.netpulse.core

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "netpulse_settings")

/**
 * Manages persistent application settings using Jetpack DataStore.
 * Provides a reactive Flow for state observation and suspend functions for updates.
 */
class SettingsStore(private val context: Context) {

    companion object {
        // Key for storing the user's preferred theme (System, Light, Dark)
        val THEME_KEY = stringPreferencesKey("theme")
    }

    // Observed theme state as a Flow
    val themeFlow: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: "System"
    }

    /**
     * Updates the theme selection in persistent storage.
     */
    suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme
        }
    }
}

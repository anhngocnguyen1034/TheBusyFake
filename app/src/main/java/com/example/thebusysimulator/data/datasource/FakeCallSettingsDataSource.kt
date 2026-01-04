package com.example.thebusysimulator.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "fake_call_settings")

class FakeCallSettingsDataSource(context: Context) {
    private val dataStore = context.dataStore

    companion object {
        private val VIBRATION_ENABLED_KEY = booleanPreferencesKey("vibration_enabled")
        private val FLASH_ENABLED_KEY = booleanPreferencesKey("flash_enabled")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
    }

    val vibrationEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[VIBRATION_ENABLED_KEY] ?: true // Default: enabled
    }

    val flashEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[FLASH_ENABLED_KEY] ?: false // Default: disabled
    }

    suspend fun setVibrationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[VIBRATION_ENABLED_KEY] = enabled
        }
    }

    suspend fun setFlashEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[FLASH_ENABLED_KEY] = enabled
        }
    }

    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE_KEY] ?: "system" // Default: system
    }

    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE_KEY] = mode
        }
    }
}


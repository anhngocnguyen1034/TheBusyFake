package com.example.thebusysimulator.data.datasource

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.languageDataStore: DataStore<Preferences> by preferencesDataStore(name = "language_settings")

class LanguageDataSource(context: Context) {
    private val dataStore = context.languageDataStore

    companion object {
        private val LANGUAGE_CODE_KEY = stringPreferencesKey("language_code")
    }

    val languageCode: Flow<String> = dataStore.data.map { preferences ->
        preferences[LANGUAGE_CODE_KEY] ?: "en" // Default: English
    }

    suspend fun setLanguageCode(code: String) {
        dataStore.edit { preferences ->
            preferences[LANGUAGE_CODE_KEY] = code
        }
    }
}

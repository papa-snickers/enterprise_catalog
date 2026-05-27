package com.example.enterprisecatalog.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "enterprise_catalog_prefs")

class DataStoreManager(private val context: Context) {

    companion object {
        private val KEY_TOKEN = stringPreferencesKey("auth_token")
        private val KEY_ROLE = stringPreferencesKey("user_role")
        private val KEY_NAME = stringPreferencesKey("user_name")
        private val KEY_EMAIL = stringPreferencesKey("user_email")
        private val KEY_DARK_THEME = booleanPreferencesKey("dark_theme")
        private val KEY_SEARCH_HISTORY = stringPreferencesKey("search_history")
    }

    // ── Auth ──────────────────────────────────────────────────────────────

    suspend fun saveAuthData(token: String, role: String, name: String, email: String) {
        context.dataStore.edit { prefs ->
            prefs[KEY_TOKEN] = token
            prefs[KEY_ROLE] = role
            prefs[KEY_NAME] = name
            prefs[KEY_EMAIL] = email
        }
    }

    suspend fun clearAuthData() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_ROLE)
            prefs.remove(KEY_NAME)
            prefs.remove(KEY_EMAIL)
        }
    }

    suspend fun getToken(): String? =
        context.dataStore.data.first()[KEY_TOKEN]

    fun getTokenFlow(): Flow<String?> =
        context.dataStore.data.map { it[KEY_TOKEN] }

    fun getRoleFlow(): Flow<String?> =
        context.dataStore.data.map { it[KEY_ROLE] }

    fun getNameFlow(): Flow<String?> =
        context.dataStore.data.map { it[KEY_NAME] }

    fun getEmailFlow(): Flow<String?> =
        context.dataStore.data.map { it[KEY_EMAIL] }

    // ── Theme ─────────────────────────────────────────────────────────────

    fun getDarkThemeFlow(): Flow<Boolean> =
        context.dataStore.data.map { it[KEY_DARK_THEME] ?: false }

    suspend fun setDarkTheme(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_THEME] = enabled }
    }

    // ── Search history ────────────────────────────────────────────────────

    fun getSearchHistoryFlow(): Flow<List<String>> =
        context.dataStore.data.map { prefs ->
            val json = prefs[KEY_SEARCH_HISTORY] ?: return@map emptyList()
            try {
                Json.decodeFromString<List<String>>(json)
            } catch (e: Exception) {
                emptyList()
            }
        }

    suspend fun addSearchQuery(query: String) {
        if (query.isBlank()) return
        context.dataStore.edit { prefs ->
            val current = try {
                Json.decodeFromString<MutableList<String>>(prefs[KEY_SEARCH_HISTORY] ?: "[]")
            } catch (e: Exception) {
                mutableListOf()
            }
            current.removeAll { it.equals(query, ignoreCase = true) }
            current.add(0, query)
            if (current.size > 10) current.subList(10, current.size).clear()
            prefs[KEY_SEARCH_HISTORY] = Json.encodeToString(current)
        }
    }

    suspend fun clearSearchHistory() {
        context.dataStore.edit { it.remove(KEY_SEARCH_HISTORY) }
    }
}

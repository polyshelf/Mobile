package com.command.polyshelf.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.command.polyshelf.utils.Logger
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStoreTokenStorage(
    private val dataStore: DataStore<Preferences>
) : TokenStorage {

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        Logger.d(
            "DataStoreTokenStorage",
            "saveTokens called. accessToken length=${accessToken.length}, refreshToken length=${refreshToken.length}"
        )
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
        Logger.d("DataStoreTokenStorage", "saveTokens completed successfully")
    }

    override suspend fun getAccessToken(): String? =
        getTokenByKey(KEY_ACCESS_TOKEN, "getAccessToken")

    override suspend fun getRefreshToken(): String? =
        getTokenByKey(KEY_REFRESH_TOKEN, "getRefreshToken")

    private suspend fun getTokenByKey(key: Preferences.Key<String>, methodName: String): String? {
        val token = dataStore.data
            .catch { emit(emptyPreferences()) }
            .map { preferences -> preferences[key] }
            .firstOrNull()
        Logger.d(
            "DataStoreTokenStorage",
            "$methodName: ${if (token.isNullOrEmpty()) "NULL/EMPTY" else "PRESENT (len=${token.length})"}"
        )
        return token
    }

    override suspend fun clear() {
        Logger.d("DataStoreTokenStorage", "clear() called -> clearing DataStore preferences")
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

    private companion object {
        val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
        val KEY_REFRESH_TOKEN = stringPreferencesKey("refresh_token")
    }
}

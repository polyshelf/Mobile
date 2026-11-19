package com.command.polyshelf.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.command.polyshelf.utils.Logger
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map

class DataStoreTokenStorage(
    private val dataStore: DataStore<Preferences>
) : TokenStorage {

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        Logger.d("DataStoreTokenStorage", "saveTokens called. accessToken length=${accessToken.length}, refreshToken length=${refreshToken.length}")
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
        }
        Logger.d("DataStoreTokenStorage", "saveTokens completed successfully")
    }

    override suspend fun getAccessToken(): String? {
        val token = dataStore.data.map { preferences ->
            preferences[KEY_ACCESS_TOKEN]
        }.firstOrNull()
        Logger.d("DataStoreTokenStorage", "getAccessToken: ${if (token.isNullOrEmpty()) "NULL/EMPTY" else "PRESENT (len=${token.length})"}")
        return token
    }

    override suspend fun getRefreshToken(): String? {
        val token = dataStore.data.map { preferences ->
            preferences[KEY_REFRESH_TOKEN]
        }.firstOrNull()
        Logger.d("DataStoreTokenStorage", "getRefreshToken: ${if (token.isNullOrEmpty()) "NULL/EMPTY" else "PRESENT (len=${token.length})"}")
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

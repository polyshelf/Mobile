package com.command.polyshelf.data.storage

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import timber.log.Timber

class AndroidDataStoreTokenStorage(
    private val dataStore: DataStore<Preferences>,
    private val keyStoreCipher: AndroidKeyStoreCipher
) : TokenStorage {

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        withContext(Dispatchers.IO) {
            Timber.d("AndroidDataStoreTokenStorage: Encrypting and saving tokens")
            val encryptedAccess = keyStoreCipher.encrypt(accessToken)
            val encryptedRefresh = keyStoreCipher.encrypt(refreshToken)

            dataStore.edit { prefs ->
                prefs[KEY_ACCESS_TOKEN] = encryptedAccess
                prefs[KEY_REFRESH_TOKEN] = encryptedRefresh
            }
        }
    }

    override suspend fun getAccessToken(): String? =
        getDecryptedTokenByKey(KEY_ACCESS_TOKEN, "access token")

    override suspend fun getRefreshToken(): String? =
        getDecryptedTokenByKey(KEY_REFRESH_TOKEN, "refresh token")

    private suspend fun getDecryptedTokenByKey(
        key: Preferences.Key<String>,
        tokenName: String
    ): String? {
        return withContext(Dispatchers.IO) {
            val encrypted = dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[key] }
                .firstOrNull() ?: return@withContext null

            val decrypted = keyStoreCipher.decrypt(encrypted)
            if (decrypted == null) {
                Timber.w("Failed to decrypt $tokenName. Clearing stored tokens.")
                clear()
            }
            decrypted
        }
    }

    override suspend fun clear() {
        withContext(Dispatchers.IO) {
            Timber.d("AndroidDataStoreTokenStorage: Clearing all tokens")
            dataStore.edit { prefs ->
                prefs.clear()
            }
        }
    }

    private companion object {
        val KEY_ACCESS_TOKEN =
            stringPreferencesKey("encrypted_access_token")
        val KEY_REFRESH_TOKEN =
            stringPreferencesKey("encrypted_refresh_token")
    }
}

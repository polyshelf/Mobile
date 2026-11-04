package com.command.polyshelf.data.storage

import android.content.Context
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.command.polyshelf.utils.Logger

class EncryptedTokenStorage(context: Context) : TokenStorage {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    override suspend fun saveTokens(accessToken: String, refreshToken: String) {
        Logger.d("EncryptedTokenStorage", "Saving KeyStore encrypted tokens. Access token len=${accessToken.length}, Refresh token len=${refreshToken.length}")
        prefs.edit {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
        }
    }

    override suspend fun getAccessToken(): String? {
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        Logger.d("EncryptedTokenStorage", "getAccessToken: ${if (token.isNullOrEmpty()) "NULL/EMPTY" else "PRESENT"}")
        return token
    }

    override suspend fun getRefreshToken(): String? {
        val token = prefs.getString(KEY_REFRESH_TOKEN, null)
        Logger.d("EncryptedTokenStorage", "getRefreshToken: ${if (token.isNullOrEmpty()) "NULL/EMPTY" else "PRESENT"}")
        return token
    }

    override suspend fun clear() {
        Logger.d("EncryptedTokenStorage", "clear() called -> clearing KeyStore encrypted tokens")
        prefs.edit { clear() }
    }

    private companion object {
        const val PREFS_NAME = "secure_polyshelf_tokens"
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}

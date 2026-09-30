package com.rusty.spotlite.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

private val Context.dataStore by preferencesDataStore(name = "spotlite_auth")

data class TokenSet(
    val accessToken: String,
    val refreshToken: String,
    val expiresAtMillis: Long,
) {
    val isExpired: Boolean get() = System.currentTimeMillis() >= expiresAtMillis - EXPIRY_SLACK_MILLIS

    companion object {
        private const val EXPIRY_SLACK_MILLIS = 60_000L
    }
}

/** Persists OAuth tokens in DataStore so login survives process death. */
class TokenStore(private val context: Context) {

    private object Keys {
        val ACCESS = stringPreferencesKey("access_token")
        val REFRESH = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
        val CODE_VERIFIER = stringPreferencesKey("pending_code_verifier")
    }

    suspend fun save(tokens: TokenSet) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACCESS] = tokens.accessToken
            prefs[Keys.REFRESH] = tokens.refreshToken
            prefs[Keys.EXPIRES_AT] = tokens.expiresAtMillis
        }
    }

    suspend fun load(): TokenSet? {
        val prefs = context.dataStore.data.first()
        val access = prefs[Keys.ACCESS] ?: return null
        val refresh = prefs[Keys.REFRESH] ?: return null
        val expiresAt = prefs[Keys.EXPIRES_AT] ?: return null
        return TokenSet(access, refresh, expiresAt)
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }

    /** Stashes the in-flight PKCE code_verifier while the user is in the browser. */
    suspend fun savePendingCodeVerifier(verifier: String) {
        context.dataStore.edit { it[Keys.CODE_VERIFIER] = verifier }
    }

    suspend fun takePendingCodeVerifier(): String? {
        val prefs = context.dataStore.data.first()
        val verifier = prefs[Keys.CODE_VERIFIER]
        context.dataStore.edit { it.remove(Keys.CODE_VERIFIER) }
        return verifier
    }
}

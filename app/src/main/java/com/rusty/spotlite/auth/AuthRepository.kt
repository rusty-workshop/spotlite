package com.rusty.spotlite.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.rusty.spotlite.Config
import com.rusty.spotlite.data.TokenSet
import com.rusty.spotlite.data.TokenStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

private const val AUTHORIZE_URL = "https://accounts.spotify.com/authorize"
private const val TOKEN_URL = "https://accounts.spotify.com/api/token"

/**
 * Handles the Spotify OAuth PKCE flow end to end: launching the login page,
 * exchanging the returned code for tokens, and refreshing access tokens.
 *
 * PKCE needs no client secret, so nothing sensitive has to live in the APK.
 */
class AuthRepository(
    private val context: Context,
    private val tokenStore: TokenStore,
    private val httpClient: OkHttpClient,
) {
    private val refreshMutex = Mutex()

    suspend fun startLogin() {
        val verifier = PkceUtil.generateCodeVerifier()
        val challenge = PkceUtil.generateCodeChallenge(verifier)
        tokenStore.savePendingCodeVerifier(verifier)

        val uri = Uri.parse(AUTHORIZE_URL).buildUpon()
            .appendQueryParameter("client_id", Config.CLIENT_ID)
            .appendQueryParameter("response_type", "code")
            .appendQueryParameter("redirect_uri", Config.REDIRECT_URI)
            .appendQueryParameter("code_challenge_method", "S256")
            .appendQueryParameter("code_challenge", challenge)
            .appendQueryParameter("scope", Config.SCOPES)
            .build()

        // AuthRepository is constructed with an application context, not an Activity —
        // any Activity-launching intent fired from that requires FLAG_ACTIVITY_NEW_TASK,
        // or startActivity() throws immediately. CustomTabsIntent.launchUrl() doesn't add
        // this itself, so it has to be set on the underlying intent here.
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, uri)
    }

    /** Call from the activity that receives the `spotlite://callback` redirect. */
    suspend fun handleRedirect(redirectUri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        val code = redirectUri.getQueryParameter("code")
            ?: return@withContext Result.failure(
                IOException(redirectUri.getQueryParameter("error") ?: "Login was cancelled")
            )
        val verifier = tokenStore.takePendingCodeVerifier()
            ?: return@withContext Result.failure(IOException("Missing PKCE verifier, restart login"))

        val body = FormBody.Builder()
            .add("grant_type", "authorization_code")
            .add("code", code)
            .add("redirect_uri", Config.REDIRECT_URI)
            .add("client_id", Config.CLIENT_ID)
            .add("code_verifier", verifier)
            .build()

        runCatching {
            val tokens = executeTokenRequest(body)
            tokenStore.save(tokens)
        }
    }

    /** Returns a valid access token, refreshing it first if it's expired. */
    suspend fun getValidAccessToken(): String? = refreshMutex.withLock {
        val current = tokenStore.load() ?: return@withLock null
        if (!current.isExpired) return@withLock current.accessToken

        val refreshed = runCatching { refresh(current.refreshToken) }.getOrNull() ?: return@withLock null
        tokenStore.save(refreshed)
        refreshed.accessToken
    }

    suspend fun isLoggedIn(): Boolean = tokenStore.load() != null

    suspend fun logout() = tokenStore.clear()

    private suspend fun refresh(refreshToken: String): TokenSet = withContext(Dispatchers.IO) {
        val body = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken)
            .add("client_id", Config.CLIENT_ID)
            .build()
        executeTokenRequest(body, fallbackRefreshToken = refreshToken)
    }

    private fun executeTokenRequest(body: FormBody, fallbackRefreshToken: String? = null): TokenSet {
        val request = Request.Builder().url(TOKEN_URL).post(body).build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Token request failed: ${response.code} ${response.body?.string()}")
            }
            val json = JSONObject(response.body?.string().orEmpty())
            val expiresInSeconds = json.getLong("expires_in")
            return TokenSet(
                accessToken = json.getString("access_token"),
                // Spotify only returns a new refresh_token sometimes; keep the old one otherwise.
                refreshToken = json.optString("refresh_token", fallbackRefreshToken.orEmpty()),
                expiresAtMillis = System.currentTimeMillis() + expiresInSeconds * 1000,
            )
        }
    }
}

package com.rusty.spotlite.auth

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/** Generates the PKCE code_verifier / code_challenge pair used by the OAuth login flow. */
object PkceUtil {

    fun generateCodeVerifier(): String {
        val bytes = ByteArray(64)
        SecureRandom().nextBytes(bytes)
        return encode(bytes)
    }

    fun generateCodeChallenge(codeVerifier: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(codeVerifier.toByteArray(Charsets.US_ASCII))
        return encode(digest)
    }

    private fun encode(bytes: ByteArray): String =
        Base64.encodeToString(bytes, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
}

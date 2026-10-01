package com.rusty.spotlite.network

import android.util.Log
import com.rusty.spotlite.auth.AuthRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.rusty.spotlite.update.GitHubApi
import retrofit2.Retrofit

private const val WEB_API_BASE_URL = "https://api.spotify.com/v1/"
private const val GITHUB_API_BASE_URL = "https://api.github.com/"
private const val LOG_TAG = "SpotliteApi"

/** Attaches a fresh bearer token to every request, refreshing it first if needed. */
private class AuthInterceptor(private val authRepository: AuthRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authRepository.getValidAccessToken() }
        // TEMPORARY — logs the full token so it can be curl-tested directly against
        // Spotify's API, bypassing our own HTTP client entirely, to isolate whether a 403
        // is coming from Spotify's side or from something in our request. Remove this
        // line in the very next commit after that test is done.
        Log.d(LOG_TAG, "${chain.request().url} -> token present: ${token != null}, token: $token")
        val request = chain.request().newBuilder().apply {
            if (token != null) addHeader("Authorization", "Bearer $token")
        }.build()
        return chain.proceed(request)
    }
}

/**
 * Logs Spotify's actual error response body on any failed request — an HttpException's
 * .message is just "HTTP 403 Forbidden", with none of the specific reason Spotify's JSON
 * error body actually explains (insufficient scope vs. invalid token vs. something else).
 * peekBody() so this doesn't consume the stream Retrofit still needs to read.
 */
private class ErrorLoggingInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val response = chain.proceed(chain.request())
        if (!response.isSuccessful) {
            val body = runCatching { response.peekBody(2048).string() }.getOrNull()
            Log.e(LOG_TAG, "${response.request.method} ${response.request.url} -> ${response.code}: $body")
        }
        return response
    }
}

object NetworkModule {

    val bareHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    fun buildSpotifyApi(authRepository: AuthRepository): SpotifyApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authRepository))
            .addInterceptor(ErrorLoggingInterceptor())
            .build()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(WEB_API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(SpotifyApi::class.java)
    }

    fun buildGitHubApi(): GitHubApi {
        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(GITHUB_API_BASE_URL)
            .client(bareHttpClient)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(GitHubApi::class.java)
    }
}

package com.rusty.spotlite.network

import com.rusty.spotlite.auth.AuthRepository
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.Retrofit

private const val WEB_API_BASE_URL = "https://api.spotify.com/v1/"

/** Attaches a fresh bearer token to every request, refreshing it first if needed. */
private class AuthInterceptor(private val authRepository: AuthRepository) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { authRepository.getValidAccessToken() }
        val request = chain.request().newBuilder().apply {
            if (token != null) addHeader("Authorization", "Bearer $token")
        }.build()
        return chain.proceed(request)
    }
}

object NetworkModule {

    val bareHttpClient: OkHttpClient by lazy { OkHttpClient.Builder().build() }

    private val json = Json { ignoreUnknownKeys = true; explicitNulls = false }

    fun buildSpotifyApi(authRepository: AuthRepository): SpotifyApi {
        val client = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(authRepository))
            .build()

        val contentType = "application/json".toMediaType()
        val retrofit = Retrofit.Builder()
            .baseUrl(WEB_API_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory(contentType))
            .build()

        return retrofit.create(SpotifyApi::class.java)
    }
}

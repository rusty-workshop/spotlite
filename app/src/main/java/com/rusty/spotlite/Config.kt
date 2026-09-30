package com.rusty.spotlite

/**
 * Create an app at https://developer.spotify.com/dashboard, add Redirect URI
 * `spotlite://callback`, then set `spotify.clientId=...` in local.properties (gitignored —
 * see app/build.gradle.kts, which reads it into BuildConfig.SPOTIFY_CLIENT_ID).
 */
object Config {
    val CLIENT_ID: String = BuildConfig.SPOTIFY_CLIENT_ID.ifBlank { "YOUR_SPOTIFY_CLIENT_ID" }
    const val REDIRECT_URI = "spotlite://callback"

    val SCOPES = listOf(
        "user-read-private",
        "user-library-read",
        "user-library-modify", // save/unsave tracks (the heart toggle)
        "user-read-playback-state", // GET /me/player/queue
        "playlist-read-private",
        "playlist-read-collaborative",
        "user-follow-read",
        "app-remote-control",
        "streaming",
    ).joinToString(" ")
}

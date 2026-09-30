package com.rusty.spotlite

/**
 * Fill these in from your app at https://developer.spotify.com/dashboard.
 * Redirect URI must be added there exactly as below (Settings -> Redirect URIs).
 */
object Config {
    const val CLIENT_ID = "YOUR_SPOTIFY_CLIENT_ID"
    const val REDIRECT_URI = "spotlite://callback"

    val SCOPES = listOf(
        "user-read-private",
        "user-library-read",
        "playlist-read-private",
        "playlist-read-collaborative",
        "user-follow-read",
        "app-remote-control",
        "streaming",
    ).joinToString(" ")
}

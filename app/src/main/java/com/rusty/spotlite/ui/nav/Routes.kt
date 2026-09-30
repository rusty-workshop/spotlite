package com.rusty.spotlite.ui.nav

import android.net.Uri

object Routes {
    const val LOGIN = "login"
    const val LIBRARY = "library"
    const val SEARCH = "search"
    const val QUEUE = "queue"
    const val PLAYLIST_PATTERN = "playlist/{playlistId}/{playlistName}/{playlistUri}"
    const val ARTIST_PATTERN = "artist/{artistId}/{artistName}"

    fun playlist(id: String, name: String, uri: String) =
        "playlist/$id/${Uri.encode(name)}/${Uri.encode(uri)}"

    fun artist(id: String, name: String) = "artist/$id/${Uri.encode(name)}"
}

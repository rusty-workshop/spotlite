package com.rusty.spotlite.ui.nav

import android.net.Uri

object Routes {
    const val LOGIN = "login"
    const val LIBRARY = "library"
    const val SEARCH = "search"
    const val PLAYLIST_PATTERN = "playlist/{playlistId}/{playlistName}"
    const val ARTIST_PATTERN = "artist/{artistId}/{artistName}"

    fun playlist(id: String, name: String) = "playlist/$id/${Uri.encode(name)}"
    fun artist(id: String, name: String) = "artist/$id/${Uri.encode(name)}"
}

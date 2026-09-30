package com.rusty.spotlite.ui.nav

import android.net.Uri

private const val NO_IMAGE = "_"

object Routes {
    const val LOGIN = "login"
    const val LIBRARY = "library"
    const val SEARCH = "search"
    const val QUEUE = "queue"
    const val PLAYLIST_PATTERN = "playlist/{playlistId}/{playlistName}/{playlistUri}/{playlistImageUrl}"
    const val ARTIST_PATTERN = "artist/{artistId}/{artistName}/{artistImageUrl}"

    fun playlist(id: String, name: String, uri: String, imageUrl: String?) =
        "playlist/$id/${Uri.encode(name)}/${Uri.encode(uri)}/${Uri.encode(imageUrl ?: NO_IMAGE)}"

    fun artist(id: String, name: String, imageUrl: String?) =
        "artist/$id/${Uri.encode(name)}/${Uri.encode(imageUrl ?: NO_IMAGE)}"

    /** Decodes a route arg produced by [playlist]/[artist]'s imageUrl back to null when absent. */
    fun decodeImageUrl(raw: String?): String? = raw?.takeIf { it.isNotBlank() && it != NO_IMAGE }
}

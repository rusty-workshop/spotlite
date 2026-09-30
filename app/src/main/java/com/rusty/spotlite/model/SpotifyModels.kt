package com.rusty.spotlite.model

import kotlinx.serialization.Serializable

@Serializable
data class ImageObject(
    val url: String,
    val height: Int? = null,
    val width: Int? = null,
)

@Serializable
data class PagingObject<T>(
    val items: List<T>,
    val next: String? = null,
    val total: Int = 0,
    val limit: Int = 0,
    val offset: Int = 0,
)

@Serializable
data class UserProfile(
    val id: String,
    val display_name: String? = null,
    val images: List<ImageObject> = emptyList(),
)

@Serializable
data class SimplifiedOwner(
    val display_name: String? = null,
)

@Serializable
data class PlaylistTracksRef(
    val total: Int = 0,
)

@Serializable
data class SimplifiedPlaylist(
    val id: String,
    val name: String,
    val images: List<ImageObject> = emptyList(),
    val owner: SimplifiedOwner? = null,
    val tracks: PlaylistTracksRef = PlaylistTracksRef(),
    val uri: String = "",
)

@Serializable
data class SimpleArtist(
    val id: String,
    val name: String,
    val uri: String = "",
)

@Serializable
data class SimpleAlbum(
    val id: String,
    val name: String,
    val images: List<ImageObject> = emptyList(),
    val uri: String = "",
)

@Serializable
data class Track(
    val id: String? = null,
    val name: String,
    val artists: List<SimpleArtist> = emptyList(),
    val album: SimpleAlbum? = null,
    val uri: String = "",
    val duration_ms: Long = 0,
) {
    val artistNames: String get() = artists.joinToString(", ") { it.name }
}

@Serializable
data class PlaylistTrackItem(
    val track: Track? = null,
)

@Serializable
data class SavedTrack(
    val track: Track,
)

@Serializable
data class Artist(
    val id: String,
    val name: String,
    val images: List<ImageObject> = emptyList(),
    val genres: List<String> = emptyList(),
    val uri: String = "",
)

@Serializable
data class Cursors(
    val after: String? = null,
)

@Serializable
data class CursorPagingObject<T>(
    val items: List<T>,
    val next: String? = null,
    val total: Int? = null,
    val cursors: Cursors? = null,
)

@Serializable
data class FollowedArtistsResponse(
    val artists: CursorPagingObject<Artist>,
)

@Serializable
data class TopTracksResponse(
    val tracks: List<Track>,
)

// Spotify's /search response only includes the keys for the types actually requested,
// so each field needs a default to cover the ones left out.
@Serializable
data class SearchResponse(
    val playlists: PagingObject<SimplifiedPlaylist>? = null,
    val artists: PagingObject<Artist>? = null,
    val tracks: PagingObject<Track>? = null,
)

@Serializable
data class QueueResponse(
    val currently_playing: Track? = null,
    val queue: List<Track> = emptyList(),
)

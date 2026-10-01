package com.rusty.spotlite.repo

import com.rusty.spotlite.model.Artist
import com.rusty.spotlite.model.SimpleAlbum
import com.rusty.spotlite.model.SimplifiedPlaylist
import com.rusty.spotlite.model.Track
import com.rusty.spotlite.network.SpotifyApi

/** One page of results plus whatever's needed to fetch the next one. */
data class Page<T>(val items: List<T>, val nextOffset: Int?)

data class CursorPage<T>(val items: List<T>, val nextCursor: String?)

data class SearchResults(
    val playlists: List<SimplifiedPlaylist>,
    val artists: List<Artist>,
    val tracks: List<Track>,
)

data class QueueState(
    val current: Track?,
    val upcoming: List<Track>,
)

private const val PAGE_SIZE = 50

/**
 * Flattens the Spotify Web API's offset/cursor pagination into a page-at-a-time
 * shape the UI's lazy lists can pull from as the user scrolls.
 */
class LibraryRepository(private val api: SpotifyApi) {

    suspend fun displayName(): String {
        val profile = api.getProfile()
        return profile.display_name ?: profile.id
    }

    suspend fun loadPlaylists(offset: Int): Page<SimplifiedPlaylist> {
        val response = api.getPlaylists(limit = PAGE_SIZE, offset = offset)
        val nextOffset = if (response.next != null) offset + PAGE_SIZE else null
        return Page(response.items, nextOffset)
    }

    suspend fun loadSavedTracks(offset: Int): Page<Track> {
        val response = api.getSavedTracks(limit = PAGE_SIZE, offset = offset)
        val nextOffset = if (response.next != null) offset + PAGE_SIZE else null
        return Page(response.items.map { it.track }, nextOffset)
    }

    suspend fun loadFollowedArtists(after: String?): CursorPage<Artist> {
        val response = api.getFollowedArtists(limit = PAGE_SIZE, after = after)
        val page = response.artists
        val nextCursor = page.cursors?.after
        return CursorPage(page.items, nextCursor)
    }

    suspend fun loadPlaylistTracks(playlistId: String, offset: Int): Page<Track> {
        val response = api.getPlaylistItems(playlistId, limit = PAGE_SIZE, offset = offset)
        val nextOffset = if (response.next != null) offset + PAGE_SIZE else null
        return Page(response.items.mapNotNull { it.track }, nextOffset)
    }

    suspend fun loadArtistTopTracks(artistId: String): List<Track> =
        api.getArtistTopTracks(artistId).tracks

    suspend fun loadArtistAlbums(artistId: String, offset: Int): Page<SimpleAlbum> {
        val response = api.getArtistAlbums(artistId, limit = PAGE_SIZE, offset = offset)
        val nextOffset = if (response.next != null) offset + PAGE_SIZE else null
        return Page(response.items, nextOffset)
    }

    suspend fun search(query: String): SearchResults {
        val response = api.search(query)
        return SearchResults(
            playlists = response.playlists?.items.orEmpty(),
            artists = response.artists?.items.orEmpty(),
            tracks = response.tracks?.items.orEmpty(),
        )
    }

    suspend fun loadQueue(): QueueState {
        val response = api.getQueue()
        return QueueState(response.currently_playing, response.queue)
    }
}

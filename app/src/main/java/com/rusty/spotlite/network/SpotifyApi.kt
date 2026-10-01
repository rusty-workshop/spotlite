package com.rusty.spotlite.network

import com.rusty.spotlite.model.FollowedArtistsResponse
import com.rusty.spotlite.model.PagingObject
import com.rusty.spotlite.model.PlaylistTrackItem
import com.rusty.spotlite.model.QueueResponse
import com.rusty.spotlite.model.SavedTrack
import com.rusty.spotlite.model.SearchResponse
import com.rusty.spotlite.model.SimpleAlbum
import com.rusty.spotlite.model.SimplifiedPlaylist
import com.rusty.spotlite.model.UserProfile
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Thin wrapper over the Spotify Web API endpoints this app reads from. */
interface SpotifyApi {

    @GET("me")
    suspend fun getProfile(): UserProfile

    @GET("me/playlists")
    suspend fun getPlaylists(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): PagingObject<SimplifiedPlaylist>

    // Spotify hard-deprecated playlists/{id}/tracks — it now returns a bare 403 for apps
    // registered after their cutover, with no scope or token issue to blame. The
    // replacement endpoint is /items, and the nested object per entry is "item", not
    // the old "track" key (confirmed against the real API response, not just docs).
    @GET("playlists/{id}/items")
    suspend fun getPlaylistItems(
        @Path("id") playlistId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("fields") fields: String = "items(item(id,name,uri,duration_ms,artists(id,name,uri),album(id,name,images))),next,total,limit,offset",
    ): PagingObject<PlaylistTrackItem>

    @GET("me/tracks")
    suspend fun getSavedTracks(
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
    ): PagingObject<SavedTrack>

    @GET("me/following")
    suspend fun getFollowedArtists(
        @Query("type") type: String = "artist",
        @Query("limit") limit: Int = 50,
        @Query("after") after: String? = null,
    ): FollowedArtistsResponse

    // getArtistTopTracks / GET artists/{id}/top-tracks removed: Spotify's Feb 2026 API
    // overhaul dropped this endpoint entirely with no replacement (confirmed against
    // Spotify's own published changelog, not assumed) — see ArtistDetailViewModel, which
    // no longer has a Top Tracks tab as a result.

    @GET("artists/{id}/albums")
    suspend fun getArtistAlbums(
        @Path("id") artistId: String,
        @Query("limit") limit: Int = 50,
        @Query("offset") offset: Int = 0,
        @Query("include_groups") includeGroups: String = "album,single",
    ): PagingObject<SimpleAlbum>

    @GET("search")
    suspend fun search(
        @Query("q") query: String,
        @Query("type") type: String = "playlist,artist,track",
        @Query("limit") limit: Int = 10,
    ): SearchResponse

    // me/tracks[/contains] was replaced by a single generic me/library endpoint covering
    // every content type (tracks, albums, shows, etc.) — it also now takes full Spotify
    // URIs ("spotify:track:xyz") instead of bare IDs, unlike the old endpoints.
    @GET("me/library/contains")
    suspend fun checkSavedTracks(@Query("uris") commaSeparatedUris: String): List<Boolean>

    // PUT requires a body even though Spotify only looks at the `uris` query param; an
    // empty JSON object satisfies OkHttp's "PUT must have a body" requirement.
    @PUT("me/library")
    suspend fun saveTracks(@Query("uris") commaSeparatedUris: String, @Body body: Map<String, String> = emptyMap())

    @DELETE("me/library")
    suspend fun removeTracks(@Query("uris") commaSeparatedUris: String)

    @GET("me/player/queue")
    suspend fun getQueue(): QueueResponse
}

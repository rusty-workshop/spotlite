package com.rusty.spotlite.network

import com.rusty.spotlite.model.FollowedArtistsResponse
import com.rusty.spotlite.model.PagingObject
import com.rusty.spotlite.model.PlaylistTrackItem
import com.rusty.spotlite.model.QueueResponse
import com.rusty.spotlite.model.SavedTrack
import com.rusty.spotlite.model.SearchResponse
import com.rusty.spotlite.model.SimpleAlbum
import com.rusty.spotlite.model.SimplifiedPlaylist
import com.rusty.spotlite.model.TopTracksResponse
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

    @GET("artists/{id}/top-tracks")
    suspend fun getArtistTopTracks(
        @Path("id") artistId: String,
        @Query("market") market: String = "from_token",
    ): TopTracksResponse

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

    @GET("me/tracks/contains")
    suspend fun checkSavedTracks(@Query("ids") commaSeparatedIds: String): List<Boolean>

    // PUT requires a body even though Spotify only looks at `ids`; an empty JSON object satisfies that.
    @PUT("me/tracks")
    suspend fun saveTracks(@Query("ids") commaSeparatedIds: String, @Body body: Map<String, String> = emptyMap())

    @DELETE("me/tracks")
    suspend fun removeTracks(@Query("ids") commaSeparatedIds: String)

    @GET("me/player/queue")
    suspend fun getQueue(): QueueResponse
}

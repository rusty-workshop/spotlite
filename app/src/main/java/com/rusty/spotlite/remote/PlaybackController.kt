package com.rusty.spotlite.remote

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rusty.spotlite.Config
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.Image
import com.spotify.protocol.types.ImageUri
import com.spotify.protocol.types.PlayerState

data class NowPlaying(
    val trackUri: String,
    val trackName: String,
    val artistName: String,
    val isPaused: Boolean,
    val albumArt: Bitmap? = null,
    /** Position as of [positionUpdatedAtMs] — App Remote only pushes updates on real
     *  events (play/pause/seek/track change), not continuously, so the UI interpolates
     *  from this anchor rather than expecting a tick every second from here. */
    val positionMs: Long = 0,
    val durationMs: Long = 0,
    val positionUpdatedAtMs: Long = System.currentTimeMillis(),
)

/**
 * Wraps the Spotify App Remote SDK: it doesn't stream audio itself, it drives
 * playback inside the real Spotify app already running on the device, which is
 * why Spotify (Premium) has to be installed alongside this app.
 */
class PlaybackController(private val context: Context) {

    private var appRemote: SpotifyAppRemote? = null

    var nowPlaying by mutableStateOf<NowPlaying?>(null)
        private set
    var isConnected by mutableStateOf(false)
        private set
    var connectionError by mutableStateOf<String?>(null)
        private set

    fun connect(onResult: (Boolean) -> Unit = {}) {
        if (appRemote?.isConnected == true) {
            onResult(true)
            return
        }
        val params = ConnectionParams.Builder(Config.CLIENT_ID)
            .setRedirectUri(Config.REDIRECT_URI)
            .showAuthView(false)
            .build()

        SpotifyAppRemote.connect(context, params, object : Connector.ConnectionListener {
            override fun onConnected(remote: SpotifyAppRemote) {
                appRemote = remote
                isConnected = true
                connectionError = null
                subscribeToPlayerState(remote)
                onResult(true)
            }

            override fun onFailure(throwable: Throwable) {
                isConnected = false
                connectionError = describe(throwable)
                onResult(false)
            }
        })
    }

    private fun describe(throwable: Throwable): String = when (throwable.javaClass.simpleName) {
        "CouldNotFindSpotifyApp" -> "Spotify isn't installed — install it to play tracks."
        "NotLoggedInException" -> "Open Spotify and log in, then try again."
        "UserNotAuthorizedException" -> "Spotify Premium is required to control playback."
        "SpotifyDisconnectedException", "SpotifyConnectionTerminatedException" -> "Lost connection to Spotify."
        else -> throwable.message ?: "Couldn't connect to Spotify."
    }

    fun disconnect() {
        appRemote?.let { SpotifyAppRemote.disconnect(it) }
        appRemote = null
        isConnected = false
        nowPlaying = null
        artLoadedForTrackUri = null
    }

    fun play(uri: String) {
        appRemote?.playerApi?.play(uri)
    }

    /** Appends a track to the Spotify app's own play queue without interrupting what's playing. */
    fun addToQueue(uri: String) {
        appRemote?.playerApi?.queue(uri)
    }

    fun togglePlayPause() {
        val playing = nowPlaying ?: return
        if (playing.isPaused) appRemote?.playerApi?.resume() else appRemote?.playerApi?.pause()
    }

    fun skipNext() {
        appRemote?.playerApi?.skipNext()
    }

    fun skipPrevious() {
        appRemote?.playerApi?.skipPrevious()
    }

    fun seekTo(positionMs: Long) {
        appRemote?.playerApi?.seekTo(positionMs)
        // Update optimistically — the SDK's own confirming event can lag visibly
        // behind a user's drag release otherwise.
        nowPlaying = nowPlaying?.copy(
            positionMs = positionMs,
            positionUpdatedAtMs = System.currentTimeMillis(),
        )
    }

    // Tracks which track's art is already loaded/loading so a slow fetch for a track the
    // user has since skipped past can't land late and overwrite the current one's art.
    private var artLoadedForTrackUri: String? = null

    private fun subscribeToPlayerState(remote: SpotifyAppRemote) {
        remote.playerApi.subscribeToPlayerState().setEventCallback { state: PlayerState ->
            val track = state.track
            val previous = nowPlaying
            nowPlaying = if (track == null) null else NowPlaying(
                trackUri = track.uri,
                trackName = track.name,
                artistName = track.artist?.name.orEmpty(),
                isPaused = state.isPaused,
                // Keep the art we already have across pause/resume/seek events for the
                // same track — only a genuine track change should trigger a refetch.
                albumArt = previous?.takeIf { it.trackUri == track.uri }?.albumArt,
                positionMs = state.playbackPosition,
                durationMs = track.duration,
                positionUpdatedAtMs = System.currentTimeMillis(),
            )
            if (track != null && track.imageUri != null && artLoadedForTrackUri != track.uri) {
                loadAlbumArt(remote, track.uri, track.imageUri)
            }
        }
    }

    private fun loadAlbumArt(remote: SpotifyAppRemote, trackUri: String, imageUri: ImageUri) {
        artLoadedForTrackUri = trackUri
        remote.imagesApi.getImage(imageUri, Image.Dimension.SMALL).setResultCallback { bitmap ->
            if (nowPlaying?.trackUri == trackUri) {
                nowPlaying = nowPlaying?.copy(albumArt = bitmap)
            }
        }
    }
}

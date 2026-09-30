package com.rusty.spotlite.remote

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.rusty.spotlite.Config
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState

data class NowPlaying(
    val trackName: String,
    val artistName: String,
    val isPaused: Boolean,
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
                subscribeToPlayerState(remote)
                onResult(true)
            }

            override fun onFailure(throwable: Throwable) {
                isConnected = false
                onResult(false)
            }
        })
    }

    fun disconnect() {
        appRemote?.let { SpotifyAppRemote.disconnect(it) }
        appRemote = null
        isConnected = false
        nowPlaying = null
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

    private fun subscribeToPlayerState(remote: SpotifyAppRemote) {
        remote.playerApi.subscribeToPlayerState().setEventCallback { state: PlayerState ->
            val track = state.track
            nowPlaying = if (track == null) null else NowPlaying(
                trackName = track.name,
                artistName = track.artist?.name.orEmpty(),
                isPaused = state.isPaused,
            )
        }
    }
}

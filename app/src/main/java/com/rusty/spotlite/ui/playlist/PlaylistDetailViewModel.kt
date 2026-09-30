package com.rusty.spotlite.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.remote.PlaybackController
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.repo.SavedTracksStore
import com.rusty.spotlite.ui.common.offsetPager
import kotlinx.coroutines.launch

class PlaylistDetailViewModel(
    repository: LibraryRepository,
    private val playbackController: PlaybackController,
    val savedTracksStore: SavedTracksStore,
    playlistId: String,
    private val playlistUri: String,
) : ViewModel() {

    val tracks = offsetPager(viewModelScope) { offset ->
        repository.loadPlaylistTracks(playlistId, offset).also { page ->
            // Fire-and-forget: rows render immediately, hearts fill in once this resolves.
            viewModelScope.launch { savedTracksStore.ensureLoaded(page.items.mapNotNull { it.id }) }
        }.let { it.items to it.nextOffset }
    }

    init {
        tracks.loadInitial()
    }

    /** Plays a single track on its own — doesn't queue the rest of the playlist after it. */
    fun playTrack(uri: String) = playbackController.play(uri)

    /** Plays the whole playlist from the top via its context URI, queueing normally. */
    fun playPlaylist() = playbackController.play(playlistUri)

    fun addToQueue(uri: String) = playbackController.addToQueue(uri)

    fun toggleSaved(trackId: String) {
        viewModelScope.launch { savedTracksStore.toggle(trackId) }
    }
}

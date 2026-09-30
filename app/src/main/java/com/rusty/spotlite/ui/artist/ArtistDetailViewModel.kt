package com.rusty.spotlite.ui.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.model.Track
import com.rusty.spotlite.remote.PlaybackController
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.repo.SavedTracksStore
import com.rusty.spotlite.ui.common.offsetPager
import kotlinx.coroutines.launch

class ArtistDetailViewModel(
    private val repository: LibraryRepository,
    private val playbackController: PlaybackController,
    val savedTracksStore: SavedTracksStore,
    private val artistId: String,
) : ViewModel() {

    var topTracks by mutableStateOf<List<Track>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    val albums = offsetPager(viewModelScope) { offset ->
        repository.loadArtistAlbums(artistId, offset).let { it.items to it.nextOffset }
    }

    init {
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            isLoading = true
            error = null
            runCatching { repository.loadArtistTopTracks(artistId) }
                .onSuccess { tracks ->
                    topTracks = tracks
                    launch { savedTracksStore.ensureLoaded(tracks.mapNotNull { it.id }) }
                }
                .onFailure { error = it.message ?: "Couldn't load this artist" }
            isLoading = false
        }
    }

    fun playTrack(uri: String) = playbackController.play(uri)

    /** Plays an album from the top via its context URI, same rationale as playing a playlist. */
    fun playAlbum(uri: String) = playbackController.play(uri)

    fun addToQueue(uri: String) = playbackController.addToQueue(uri)

    fun loadAlbumsIfNeeded() = albums.loadInitial()

    fun toggleSaved(trackId: String) {
        viewModelScope.launch { savedTracksStore.toggle(trackId) }
    }
}

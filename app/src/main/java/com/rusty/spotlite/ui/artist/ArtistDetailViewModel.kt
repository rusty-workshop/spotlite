package com.rusty.spotlite.ui.artist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.model.Track
import com.rusty.spotlite.remote.PlaybackController
import com.rusty.spotlite.repo.LibraryRepository
import kotlinx.coroutines.launch

class ArtistDetailViewModel(
    private val repository: LibraryRepository,
    private val playbackController: PlaybackController,
    private val artistId: String,
) : ViewModel() {

    var topTracks by mutableStateOf<List<Track>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            isLoading = true
            topTracks = runCatching { repository.loadArtistTopTracks(artistId) }.getOrDefault(emptyList())
            isLoading = false
        }
    }

    fun playTrack(uri: String) = playbackController.play(uri)
}

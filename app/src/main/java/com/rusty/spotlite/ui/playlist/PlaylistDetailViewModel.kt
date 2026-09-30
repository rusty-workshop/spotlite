package com.rusty.spotlite.ui.playlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.remote.PlaybackController
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.ui.common.offsetPager

class PlaylistDetailViewModel(
    repository: LibraryRepository,
    private val playbackController: PlaybackController,
    private val playlistId: String,
) : ViewModel() {

    val tracks = offsetPager(viewModelScope) { offset ->
        repository.loadPlaylistTracks(playlistId, offset).let { it.items to it.nextOffset }
    }

    init {
        tracks.loadInitial()
    }

    fun playTrack(uri: String) = playbackController.play(uri)
}

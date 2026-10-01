package com.rusty.spotlite.ui.artist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.remote.PlaybackController
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.ui.common.offsetPager

// Top Tracks used to live here too, but Spotify's Feb 2026 API overhaul removed
// GET /artists/{id}/top-tracks entirely with no replacement — see SpotifyApi.kt.
class ArtistDetailViewModel(
    repository: LibraryRepository,
    private val playbackController: PlaybackController,
    artistId: String,
) : ViewModel() {

    val albums = offsetPager(viewModelScope) { offset ->
        repository.loadArtistAlbums(artistId, offset).let { it.items to it.nextOffset }
    }

    init {
        albums.loadInitial()
    }

    /** Plays an album from the top via its context URI, same rationale as playing a playlist. */
    fun playAlbum(uri: String) = playbackController.play(uri)
}

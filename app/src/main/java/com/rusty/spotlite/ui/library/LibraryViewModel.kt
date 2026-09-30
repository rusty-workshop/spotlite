package com.rusty.spotlite.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.ui.common.OffsetPager
import com.rusty.spotlite.ui.common.offsetPager

class LibraryViewModel(repository: LibraryRepository) : ViewModel() {

    val playlists = offsetPager(viewModelScope) { offset ->
        repository.loadPlaylists(offset).let { it.items to it.nextOffset }
    }

    val savedTracks = offsetPager(viewModelScope) { offset ->
        repository.loadSavedTracks(offset).let { it.items to it.nextOffset }
    }

    val followedArtists = OffsetPager(viewModelScope) { cursor ->
        repository.loadFollowedArtists(cursor).let { it.items to it.nextCursor }
    }

    fun loadInitialIfNeeded(tab: LibraryTab) {
        when (tab) {
            LibraryTab.PLAYLISTS -> playlists.loadInitial()
            LibraryTab.LIKED_SONGS -> savedTracks.loadInitial()
            LibraryTab.ARTISTS -> followedArtists.loadInitial()
        }
    }
}

enum class LibraryTab(val label: String) {
    PLAYLISTS("Playlists"),
    LIKED_SONGS("Liked Songs"),
    ARTISTS("Artists"),
}

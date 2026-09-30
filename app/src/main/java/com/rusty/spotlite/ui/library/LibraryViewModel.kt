package com.rusty.spotlite.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.repo.SavedTracksStore
import com.rusty.spotlite.ui.common.OffsetPager
import com.rusty.spotlite.ui.common.offsetPager
import kotlinx.coroutines.launch

class LibraryViewModel(
    repository: LibraryRepository,
    val savedTracksStore: SavedTracksStore,
) : ViewModel() {

    val playlists = offsetPager(viewModelScope) { offset ->
        repository.loadPlaylists(offset).let { it.items to it.nextOffset }
    }

    val savedTracks = offsetPager(viewModelScope) { offset ->
        // Every track here is by definition already liked — no need to ask the API.
        repository.loadSavedTracks(offset).also { page ->
            savedTracksStore.markKnownSaved(page.items.mapNotNull { it.id })
        }.let { it.items to it.nextOffset }
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

    fun toggleSaved(trackId: String) {
        viewModelScope.launch { savedTracksStore.toggle(trackId) }
    }
}

enum class LibraryTab(val label: String) {
    PLAYLISTS("Playlists"),
    LIKED_SONGS("Liked Songs"),
    ARTISTS("Artists"),
}

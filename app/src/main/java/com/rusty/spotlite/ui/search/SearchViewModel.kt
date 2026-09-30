package com.rusty.spotlite.ui.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.repo.SavedTracksStore
import com.rusty.spotlite.repo.SearchResults
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val DEBOUNCE_MILLIS = 300L

class SearchViewModel(
    private val repository: LibraryRepository,
    val savedTracksStore: SavedTracksStore,
) : ViewModel() {

    var query by mutableStateOf("")
        private set
    var results by mutableStateOf<SearchResults?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    private var searchJob: Job? = null

    fun onQueryChange(newQuery: String) {
        query = newQuery
        searchJob?.cancel()

        if (newQuery.isBlank()) {
            results = null
            isLoading = false
            error = null
            return
        }

        // Debounced so typing doesn't fire a request per keystroke — only the
        // request that survives 300ms of silence actually hits the network.
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            isLoading = true
            error = null
            runCatching { repository.search(newQuery) }
                .onSuccess { found ->
                    results = found
                    launch { savedTracksStore.ensureLoaded(found.tracks.mapNotNull { it.id }) }
                }
                .onFailure { error = it.message ?: "Search failed" }
            isLoading = false
        }
    }

    fun retry() = onQueryChange(query)

    fun toggleSaved(trackId: String) {
        viewModelScope.launch { savedTracksStore.toggle(trackId) }
    }
}

package com.rusty.spotlite.ui.queue

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.repo.LibraryRepository
import com.rusty.spotlite.repo.QueueState
import kotlinx.coroutines.launch

/**
 * The Web API's queue is a snapshot, not a stream — there's no push update when it
 * changes, so this loads once on open and again on explicit refresh rather than polling
 * (which would cost battery/data for no real benefit on a screen people check briefly).
 */
class QueueViewModel(private val repository: LibraryRepository) : ViewModel() {

    var state by mutableStateOf<QueueState?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            isLoading = true
            error = null
            runCatching { repository.loadQueue() }
                .onSuccess { state = it }
                .onFailure { error = it.message ?: "Couldn't load the queue" }
            isLoading = false
        }
    }
}

package com.rusty.spotlite.ui.common

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Minimal offset/cursor-based incremental loader for a Compose LazyColumn.
 * Deliberately avoids Paging3: these lists are simple REST pages, and a hand-rolled
 * loader keeps the dependency graph (and APK) smaller for a "runs well on low-end
 * phones" app without giving up scroll-triggered incremental loading.
 */
class OffsetPager<T>(
    private val scope: CoroutineScope,
    private val loadPage: suspend (cursor: String?) -> Pair<List<T>, String?>,
) {
    var items by mutableStateOf<List<T>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var hasMore by mutableStateOf(true)
        private set

    private var cursor: String? = null
    private var loadedOnce = false

    fun loadInitial() {
        if (loadedOnce) return
        loadedOnce = true
        loadMore()
    }

    fun refresh() {
        items = emptyList()
        cursor = null
        hasMore = true
        error = null
        loadedOnce = true
        loadMore()
    }

    fun loadMore() {
        if (isLoading || !hasMore) return
        isLoading = true
        error = null
        scope.launch {
            runCatching { loadPage(cursor) }
                .onSuccess { (newItems, nextCursor) ->
                    items = items + newItems
                    cursor = nextCursor
                    hasMore = nextCursor != null
                }
                .onFailure { error = it.message ?: "Something went wrong" }
            isLoading = false
        }
    }
}

/** Adapts an offset-style [Page]/[CursorPage] loader into the (cursor) -> (items, next) shape. */
fun <T> offsetPager(scope: CoroutineScope, loadPage: suspend (offset: Int) -> Pair<List<T>, Int?>) =
    OffsetPager(scope) { cursor: String? ->
        val offset = cursor?.toIntOrNull() ?: 0
        val (items, next) = loadPage(offset)
        items to next?.toString()
    }

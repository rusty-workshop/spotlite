package com.rusty.spotlite.ui.common

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow

/** Fires [onLoadMore] once the user scrolls within a few rows of the end of the list. */
@Composable
fun InfiniteScrollHandler(listState: LazyListState, itemCount: Int, onLoadMore: () -> Unit) {
    val shouldLoadMore = remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            itemCount > 0 && lastVisible >= itemCount - 5
        }
    }
    LaunchedEffect(listState, itemCount) {
        snapshotFlow { shouldLoadMore.value }.collect { if (it) onLoadMore() }
    }
}

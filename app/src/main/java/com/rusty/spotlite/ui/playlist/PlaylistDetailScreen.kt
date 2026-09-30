package com.rusty.spotlite.ui.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.rusty.spotlite.ui.common.ErrorRow
import com.rusty.spotlite.ui.common.InfiniteScrollHandler
import com.rusty.spotlite.ui.common.LoadingRow
import com.rusty.spotlite.ui.common.TrackRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    viewModel: PlaylistDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(playlistName, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        val pager = viewModel.tracks
        val listState = rememberLazyListState()
        InfiniteScrollHandler(listState, pager.items.size, pager::loadMore)

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            items(pager.items, key = { it.id ?: it.uri }) { track ->
                TrackRow(track, onClick = { viewModel.playTrack(track.uri) })
            }
            if (pager.isLoading) item { LoadingRow() }
            pager.error?.let { message -> item { ErrorRow(message, onRetry = pager::loadMore) } }
        }
    }
}

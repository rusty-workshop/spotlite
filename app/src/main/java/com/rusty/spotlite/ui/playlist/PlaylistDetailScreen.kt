package com.rusty.spotlite.ui.playlist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.ui.common.ErrorRow
import com.rusty.spotlite.ui.common.HeroImage
import com.rusty.spotlite.ui.common.InfiniteScrollHandler
import com.rusty.spotlite.ui.common.LoadingRow
import com.rusty.spotlite.ui.common.TrackRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistDetailScreen(
    playlistName: String,
    playlistImageUrl: String?,
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
            actions = {
                // Starts from the top of the playlist and queues normally — App Remote has
                // no reliable way to start mid-playlist at an arbitrary track, so tapping a
                // row below still only plays that one track on its own.
                IconButton(onClick = viewModel::playPlaylist) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = "Play playlist")
                }
            },
        )

        val pager = viewModel.tracks
        val listState = rememberLazyListState()
        InfiniteScrollHandler(listState, pager.items.size, pager::loadMore)

        LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    HeroImage(playlistImageUrl)
                    Text(
                        text = playlistName,
                        style = MaterialTheme.typography.titleLarge,
                        textAlign = TextAlign.Center,
                        maxLines = 2,
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
            }
            items(pager.items, key = { it.id ?: it.uri }) { track ->
                val trackId = track.id
                TrackRow(
                    track = track,
                    onClick = { viewModel.playTrack(track.uri) },
                    isSaved = trackId?.let { viewModel.savedTracksStore.isSavedOrNull(it) },
                    onToggleSave = trackId?.let { id -> { viewModel.toggleSaved(id) } },
                    onAddToQueue = { viewModel.addToQueue(track.uri) },
                )
            }
            if (pager.isLoading) item { LoadingRow() }
            pager.error?.let { message -> item { ErrorRow(message, onRetry = pager::loadMore) } }
        }
    }
}

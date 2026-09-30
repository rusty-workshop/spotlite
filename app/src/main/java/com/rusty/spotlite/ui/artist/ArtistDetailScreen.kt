package com.rusty.spotlite.ui.artist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.ui.common.AlbumRow
import com.rusty.spotlite.ui.common.ErrorRow
import com.rusty.spotlite.ui.common.HeroImage
import com.rusty.spotlite.ui.common.InfiniteScrollHandler
import com.rusty.spotlite.ui.common.LoadingRow
import com.rusty.spotlite.ui.common.TrackRow

private enum class ArtistTab(val label: String) {
    TOP_TRACKS("Top Tracks"),
    ALBUMS("Albums"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    artistImageUrl: String?,
    viewModel: ArtistDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(ArtistTab.TOP_TRACKS) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(artistName, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )
        Column(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            HeroImage(artistImageUrl, size = 120.dp, shape = CircleShape)
            Text(
                text = artistName,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
                maxLines = 2,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
        PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
            ArtistTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) },
                )
            }
        }

        LaunchedEffect(selectedTab) {
            if (selectedTab == ArtistTab.ALBUMS) viewModel.loadAlbumsIfNeeded()
        }

        when (selectedTab) {
            ArtistTab.TOP_TRACKS -> {
                val error = viewModel.error
                when {
                    viewModel.isLoading -> Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        CircularProgressIndicator()
                    }

                    error != null -> ErrorRow(error, onRetry = viewModel::retry)

                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(viewModel.topTracks, key = { it.id ?: it.uri }) { track ->
                            val trackId = track.id
                            TrackRow(
                                track = track,
                                onClick = { viewModel.playTrack(track.uri) },
                                isSaved = trackId?.let { viewModel.savedTracksStore.isSavedOrNull(it) },
                                onToggleSave = trackId?.let { id -> { viewModel.toggleSaved(id) } },
                                onAddToQueue = { viewModel.addToQueue(track.uri) },
                            )
                        }
                    }
                }
            }

            ArtistTab.ALBUMS -> {
                val pager = viewModel.albums
                val listState = rememberLazyListState()
                InfiniteScrollHandler(listState, pager.items.size, pager::loadMore)
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(pager.items, key = { it.id }) { album ->
                        AlbumRow(album, onClick = { viewModel.playAlbum(album.uri) })
                    }
                    if (pager.isLoading) item { LoadingRow() }
                    pager.error?.let { message -> item { ErrorRow(message, onRetry = pager::loadMore) } }
                }
            }
        }
    }
}

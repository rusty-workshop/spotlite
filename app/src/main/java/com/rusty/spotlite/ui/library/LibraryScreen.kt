package com.rusty.spotlite.ui.library

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.model.Artist
import com.rusty.spotlite.model.SimplifiedPlaylist
import com.rusty.spotlite.ui.common.InfiniteScrollHandler
import com.rusty.spotlite.ui.common.Thumbnail
import com.rusty.spotlite.ui.common.TrackRow

@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onOpenPlaylist: (SimplifiedPlaylist) -> Unit,
    onOpenArtist: (Artist) -> Unit,
    onPlayTrackUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.PLAYLISTS) }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab.ordinal) {
            LibraryTab.entries.forEach { tab ->
                Tab(
                    selected = selectedTab == tab,
                    onClick = { selectedTab = tab },
                    text = { Text(tab.label) },
                )
            }
        }

        LaunchedEffect(selectedTab) { viewModel.loadInitialIfNeeded(selectedTab) }

        when (selectedTab) {
            LibraryTab.PLAYLISTS -> {
                val pager = viewModel.playlists
                val listState = rememberLazyListState()
                InfiniteScrollHandler(listState, pager.items.size, pager::loadMore)
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(pager.items, key = { it.id }) { playlist ->
                        PlaylistRow(playlist, onClick = { onOpenPlaylist(playlist) })
                    }
                    if (pager.isLoading) item { LoadingRow() }
                }
            }

            LibraryTab.LIKED_SONGS -> {
                val pager = viewModel.savedTracks
                val listState = rememberLazyListState()
                InfiniteScrollHandler(listState, pager.items.size, pager::loadMore)
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(pager.items, key = { it.id ?: it.uri }) { track ->
                        TrackRow(track, onClick = { onPlayTrackUri(track.uri) })
                    }
                    if (pager.isLoading) item { LoadingRow() }
                }
            }

            LibraryTab.ARTISTS -> {
                val pager = viewModel.followedArtists
                val listState = rememberLazyListState()
                InfiniteScrollHandler(listState, pager.items.size, pager::loadMore)
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(pager.items, key = { it.id }) { artist ->
                        ArtistRow(artist, onClick = { onOpenArtist(artist) })
                    }
                    if (pager.isLoading) item { LoadingRow() }
                }
            }
        }
    }
}

@Composable
private fun PlaylistRow(playlist: SimplifiedPlaylist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Thumbnail(images = playlist.images)
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(playlist.name, style = MaterialTheme.typography.bodyLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${playlist.tracks.total} songs",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun ArtistRow(artist: Artist, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Thumbnail(images = artist.images)
        Text(artist.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}

@Composable
private fun LoadingRow() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        horizontalArrangement = Arrangement.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(8.dp))
    }
}

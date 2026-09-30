package com.rusty.spotlite.ui.library

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.Modifier
import com.rusty.spotlite.model.Artist
import com.rusty.spotlite.model.SimplifiedPlaylist
import com.rusty.spotlite.ui.common.ErrorRow
import com.rusty.spotlite.ui.common.InfiniteScrollHandler
import com.rusty.spotlite.ui.common.ArtistRow
import com.rusty.spotlite.ui.common.LoadingRow
import com.rusty.spotlite.ui.common.PlaylistRow
import com.rusty.spotlite.ui.common.TrackRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    onOpenPlaylist: (SimplifiedPlaylist) -> Unit,
    onOpenArtist: (Artist) -> Unit,
    onPlayTrackUri: (String) -> Unit,
    onAddToQueueUri: (String) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenSettings: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var selectedTab by remember { mutableStateOf(LibraryTab.PLAYLISTS) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Column {
                    Text("Spotlite")
                    viewModel.displayName?.let { name ->
                        Text(
                            text = "Hi, $name",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = onOpenSearch) {
                    Icon(Icons.Filled.Search, contentDescription = "Search")
                }
                IconButton(onClick = onOpenSettings) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings")
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Log out")
                }
            },
        )
        PrimaryTabRow(selectedTabIndex = selectedTab.ordinal) {
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
                    pager.error?.let { message -> item { ErrorRow(message, onRetry = pager::loadMore) } }
                }
            }

            LibraryTab.LIKED_SONGS -> {
                val pager = viewModel.savedTracks
                val listState = rememberLazyListState()
                InfiniteScrollHandler(listState, pager.items.size, pager::loadMore)
                LazyColumn(state = listState, modifier = Modifier.fillMaxSize()) {
                    items(pager.items, key = { it.id ?: it.uri }) { track ->
                        val trackId = track.id
                        TrackRow(
                            track = track,
                            onClick = { onPlayTrackUri(track.uri) },
                            isSaved = trackId?.let { viewModel.savedTracksStore.isSavedOrNull(it) },
                            onToggleSave = trackId?.let { id -> { viewModel.toggleSaved(id) } },
                            onAddToQueue = { onAddToQueueUri(track.uri) },
                        )
                    }
                    if (pager.isLoading) item { LoadingRow() }
                    pager.error?.let { message -> item { ErrorRow(message, onRetry = pager::loadMore) } }
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
                    pager.error?.let { message -> item { ErrorRow(message, onRetry = pager::loadMore) } }
                }
            }
        }
    }
}

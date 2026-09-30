package com.rusty.spotlite.ui.search

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.rusty.spotlite.model.Artist
import com.rusty.spotlite.model.SimplifiedPlaylist
import com.rusty.spotlite.model.Track
import com.rusty.spotlite.ui.common.ArtistRow
import com.rusty.spotlite.ui.common.ErrorRow
import com.rusty.spotlite.ui.common.LoadingRow
import com.rusty.spotlite.ui.common.PlaylistRow
import com.rusty.spotlite.ui.common.TrackRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: SearchViewModel,
    onBack: () -> Unit,
    onOpenPlaylist: (SimplifiedPlaylist) -> Unit,
    onOpenArtist: (Artist) -> Unit,
    onPlayTrackUri: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusRequester = remember { FocusRequester() }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                OutlinedTextField(
                    value = viewModel.query,
                    onValueChange = viewModel::onQueryChange,
                    placeholder = { Text("Search playlists, artists, songs") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
        )

        LaunchedEffect(Unit) { focusRequester.requestFocus() }

        val results = viewModel.results
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (viewModel.isLoading) item { LoadingRow() }
            viewModel.error?.let { message -> item { ErrorRow(message, onRetry = viewModel::retry) } }

            if (results != null) {
                if (results.playlists.isEmpty() && results.artists.isEmpty() && results.tracks.isEmpty() && !viewModel.isLoading) {
                    item { SectionHeader("No results for \"${viewModel.query}\"") }
                }
                if (results.playlists.isNotEmpty()) {
                    item { SectionHeader("Playlists") }
                    items(results.playlists, key = { "pl_${it.id}" }) { playlist ->
                        PlaylistRow(playlist, onClick = { onOpenPlaylist(playlist) })
                    }
                }
                if (results.artists.isNotEmpty()) {
                    item { SectionHeader("Artists") }
                    items(results.artists, key = { "ar_${it.id}" }) { artist ->
                        ArtistRow(artist, onClick = { onOpenArtist(artist) })
                    }
                }
                if (results.tracks.isNotEmpty()) {
                    item { SectionHeader("Songs") }
                    items(results.tracks, key = { "tr_${it.id ?: it.uri}" }) { track: Track ->
                        TrackRow(track, onClick = { onPlayTrackUri(track.uri) })
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
    )
}

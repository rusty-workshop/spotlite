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
import com.rusty.spotlite.ui.common.AlbumRow
import com.rusty.spotlite.ui.common.ErrorRow
import com.rusty.spotlite.ui.common.HeroImage
import com.rusty.spotlite.ui.common.InfiniteScrollHandler
import com.rusty.spotlite.ui.common.LoadingRow

// Top Tracks used to be a second tab here, but Spotify removed the underlying
// GET /artists/{id}/top-tracks endpoint in its Feb 2026 API overhaul with no
// replacement — see ArtistDetailViewModel. Albums is all that's left to show.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArtistDetailScreen(
    artistName: String,
    artistImageUrl: String?,
    viewModel: ArtistDetailViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
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

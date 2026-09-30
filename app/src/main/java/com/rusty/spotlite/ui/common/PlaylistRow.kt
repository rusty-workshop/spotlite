package com.rusty.spotlite.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.model.SimplifiedPlaylist

@Composable
fun PlaylistRow(playlist: SimplifiedPlaylist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
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

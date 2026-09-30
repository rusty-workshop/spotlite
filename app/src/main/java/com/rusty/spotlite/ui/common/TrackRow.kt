package com.rusty.spotlite.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.model.Track

/**
 * [isSaved] is null until the heart's saved-status has actually been fetched (see
 * SavedTracksStore) — the heart only renders once we know which icon to show, rather
 * than flashing a wrong state. [onToggleSave] is null wherever a track has no id to
 * save against (Spotify sometimes omits it on local/unavailable tracks). [onAddToQueue]
 * is null wherever queueing doesn't make sense (e.g. inside the queue screen itself).
 */
@Composable
fun TrackRow(
    track: Track,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isSaved: Boolean? = null,
    onToggleSave: (() -> Unit)? = null,
    onAddToQueue: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Thumbnail(images = track.album?.images.orEmpty())
        Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
            Text(
                text = track.name,
                style = MaterialTheme.typography.bodyLarge,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = track.artistNames,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (onAddToQueue != null) {
            IconButton(onClick = onAddToQueue) {
                Icon(Icons.AutoMirrored.Filled.PlaylistAdd, contentDescription = "Add to queue")
            }
        }
        if (onToggleSave != null && isSaved != null) {
            IconButton(onClick = onToggleSave) {
                Icon(
                    if (isSaved) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isSaved) "Remove from Liked Songs" else "Save to Liked Songs",
                    tint = if (isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

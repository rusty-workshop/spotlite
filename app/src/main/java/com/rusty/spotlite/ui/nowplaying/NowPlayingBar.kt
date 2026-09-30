package com.rusty.spotlite.ui.nowplaying

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.remote.NowPlaying
import kotlinx.coroutines.delay

// A static shape + Surface elevation reads as a "card floating above the list" for the
// cost of one clip and one shadow draw per frame — negligible next to a scrolling list.
private val NowPlayingBarShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
private const val ART_SIZE_DP = 40
private const val TICK_MILLIS = 500L

@Composable
fun NowPlayingBar(
    nowPlaying: NowPlaying,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onOpenQueue: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = NowPlayingBarShape,
        color = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            ProgressRow(nowPlaying, onSeek)

            Row(verticalAlignment = Alignment.CenterVertically) {
                MiniArt(nowPlaying.albumArt, modifier = Modifier.padding(end = 12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = nowPlaying.trackName,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = nowPlaying.artistName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                IconButton(onClick = onTogglePlayPause) {
                    Icon(
                        if (nowPlaying.isPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                        contentDescription = if (nowPlaying.isPaused) "Play" else "Pause",
                    )
                }
                IconButton(onClick = onSkipNext) {
                    Icon(Icons.Filled.SkipNext, contentDescription = "Skip")
                }
                IconButton(onClick = onOpenQueue) {
                    Icon(Icons.AutoMirrored.Filled.QueueMusic, contentDescription = "Queue")
                }
            }
        }
    }
}

/**
 * App Remote only pushes a position update on real events (play/pause/seek/track
 * change) — not once a second — so this interpolates locally from the last known
 * anchor while playing, and resyncs to the authoritative value whenever a fresh event
 * actually arrives (the LaunchedEffect key below).
 */
@Composable
private fun ProgressRow(nowPlaying: NowPlaying, onSeek: (Long) -> Unit) {
    var isDragging by remember { mutableStateOf(false) }
    var dragPositionMs by remember { mutableLongStateOf(0L) }
    var tickingPositionMs by remember { mutableLongStateOf(nowPlaying.positionMs) }

    LaunchedEffect(nowPlaying.positionMs, nowPlaying.positionUpdatedAtMs, nowPlaying.isPaused) {
        tickingPositionMs = nowPlaying.positionMs
        while (!nowPlaying.isPaused) {
            delay(TICK_MILLIS)
            val elapsedSinceAnchor = System.currentTimeMillis() - nowPlaying.positionUpdatedAtMs
            tickingPositionMs = (nowPlaying.positionMs + elapsedSinceAnchor).coerceAtMost(nowPlaying.durationMs)
        }
    }

    val durationMs = nowPlaying.durationMs.coerceAtLeast(1L)
    val displayedPositionMs = if (isDragging) dragPositionMs else tickingPositionMs

    Slider(
        value = displayedPositionMs.toFloat(),
        onValueChange = {
            isDragging = true
            dragPositionMs = it.toLong()
        },
        onValueChangeFinished = {
            onSeek(dragPositionMs)
            isDragging = false
        },
        valueRange = 0f..durationMs.toFloat(),
        colors = SliderDefaults.colors(activeTrackColor = MaterialTheme.colorScheme.primary),
    )
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = formatDuration(displayedPositionMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
        )
        Text(
            text = formatDuration(nowPlaying.durationMs),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
        )
    }
}

private fun formatDuration(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return "%d:%02d".format(minutes, seconds)
}

/**
 * Renders the current track's art directly from the Bitmap App Remote hands back — no
 * Coil involved, since this isn't a URL, it's a one-off in-memory image from the SDK.
 */
@Composable
private fun MiniArt(bitmap: Bitmap?, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(ART_SIZE_DP.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center,
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(ART_SIZE_DP.dp),
            )
        } else {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.Gray)
        }
    }
}

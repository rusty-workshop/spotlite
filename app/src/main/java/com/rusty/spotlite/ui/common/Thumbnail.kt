package com.rusty.spotlite.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import com.rusty.spotlite.model.ImageObject

private const val THUMB_SIZE_DP = 48

/**
 * A small, fixed-size thumbnail. Requests the smallest image Spotify offers for the
 * item (rather than downscaling their largest) and caps Coil's decode target, since
 * decoding full-resolution album art for a 48dp row is the single easiest way to
 * make a list feel slow on a low-end phone.
 */
@Composable
fun Thumbnail(images: List<ImageObject>, modifier: Modifier = Modifier) {
    val url = images.minByOrNull { it.width ?: Int.MAX_VALUE }?.url
    Box(
        modifier = modifier
            .size(THUMB_SIZE_DP.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (url != null) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(url)
                    .size(THUMB_SIZE_DP * 2) // ~2x for density, never full-res
                    // Only costs anything on a cache miss; cached rows (the common
                    // case while scrolling) render instantly regardless.
                    .crossfade(150)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(THUMB_SIZE_DP.dp),
            )
        } else {
            Icon(
                Icons.Filled.MusicNote,
                contentDescription = null,
                tint = Color.Gray,
            )
        }
    }
}

package com.rusty.spotlite.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

private val DEFAULT_SIZE = 160.dp

/**
 * A larger cover for a playlist/artist/album header — still capped well below the
 * source image's full resolution, just a bigger cap than the list rows use.
 */
@Composable
fun HeroImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
    size: Dp = DEFAULT_SIZE,
    shape: Shape = RoundedCornerShape(12.dp),
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (imageUrl != null) {
            val context = LocalContext.current
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(imageUrl)
                    .size((size.value * 2).toInt())
                    .crossfade(150)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(size),
            )
        } else {
            Icon(Icons.Filled.MusicNote, contentDescription = null, tint = Color.Gray)
        }
    }
}

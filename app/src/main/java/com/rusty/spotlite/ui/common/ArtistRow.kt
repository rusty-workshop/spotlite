package com.rusty.spotlite.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.rusty.spotlite.model.Artist

@Composable
fun ArtistRow(artist: Artist, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Thumbnail(images = artist.images)
        Text(artist.name, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(start = 12.dp))
    }
}

package com.rusty.spotlite.repo

import androidx.compose.runtime.mutableStateMapOf
import com.rusty.spotlite.network.SpotifyApi

// Spotify's generic /me/library endpoints cap at 40 URIs per request (the old
// track-specific endpoints allowed 50 bare IDs).
private const val CONTAINS_BATCH_SIZE = 40

/**
 * Tracks which track IDs are in the user's Liked Songs, shared across every screen (via
 * AppContainer) so a heart toggled in Search reflects the same state if that track later
 * shows up in a playlist. Backed by a Compose snapshot map so any row reading it recomposes
 * on change without a ViewModel/Flow round trip.
 */
class SavedTracksStore(private val api: SpotifyApi) {
    private val saved = mutableStateMapOf<String, Boolean>()

    fun isSavedOrNull(trackId: String): Boolean? = saved[trackId]

    /**
     * Fetches saved-status for whichever of [trackIds] aren't already known, one batched
     * `contains` call per unknown chunk rather than one call per row.
     */
    suspend fun ensureLoaded(trackIds: List<String>) {
        val unknown = trackIds.filter { it !in saved }.distinct()
        if (unknown.isEmpty()) return
        unknown.chunked(CONTAINS_BATCH_SIZE).forEach { chunk ->
            runCatching { api.checkSavedTracks(chunk.joinToString(",") { trackUri(it) }) }
                .onSuccess { result -> chunk.zip(result).forEach { (id, isSaved) -> saved[id] = isSaved } }
        }
    }

    /** For contexts (like Liked Songs itself) where saved-ness is already known — no network call. */
    fun markKnownSaved(trackIds: List<String>) {
        trackIds.forEach { saved[it] = true }
    }

    suspend fun toggle(trackId: String) {
        val wasSaved = saved[trackId] ?: false
        saved[trackId] = !wasSaved // optimistic
        runCatching {
            val uri = trackUri(trackId)
            if (wasSaved) api.removeTracks(uri) else api.saveTracks(uri)
        }.onFailure {
            saved[trackId] = wasSaved // revert
        }
    }

    // /me/library's endpoints take full Spotify URIs, not bare IDs — deterministic for
    // tracks, so no need to thread the actual Track.uri field through every call site.
    private fun trackUri(trackId: String) = "spotify:track:$trackId"
}

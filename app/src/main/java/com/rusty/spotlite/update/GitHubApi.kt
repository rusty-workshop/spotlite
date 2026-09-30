package com.rusty.spotlite.update

import kotlinx.serialization.Serializable
import retrofit2.http.GET

@Serializable
data class GitHubAsset(
    val name: String,
    val browser_download_url: String,
)

@Serializable
data class GitHubRelease(
    val tag_name: String,
    val assets: List<GitHubAsset> = emptyList(),
)

interface GitHubApi {
    // GitHub's own notion of "latest" here is just the most recently published release by
    // any tag — the release workflow (.github/workflows/release.yml) tags each one v<N>
    // where N is the commit count, which this endpoint always resolves to the newest of.
    @GET("repos/rusty-workshop/spotlite/releases/latest")
    suspend fun getLatestRelease(): GitHubRelease
}

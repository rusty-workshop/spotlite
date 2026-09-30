package com.rusty.spotlite.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.core.content.FileProvider
import com.rusty.spotlite.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

private val CHECK_INTERVAL_MILLIS = java.util.concurrent.TimeUnit.HOURS.toMillis(6)

/**
 * Checks GitHub for a newer release than what's installed, downloads it, and launches the
 * system installer automatically. Android gives a normal (non-Play-Store) app no way to
 * install a new APK without the user confirming the system's install dialog — this gets
 * everything else out of the user's way so that tap is the only manual step left.
 */
class UpdateManager(
    private val context: Context,
    private val api: GitHubApi,
    private val httpClient: OkHttpClient,
) {
    private val _toastMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val toastMessages: SharedFlow<String> = _toastMessages.asSharedFlow()

    private var lastCheckAtMillis = 0L

    /** [manual] skips the throttle and also reports "already up to date" / failures — a
     *  background check on every app launch stays silent unless it actually finds something. */
    suspend fun checkForUpdate(manual: Boolean = false) = withContext(Dispatchers.IO) {
        if (!manual && System.currentTimeMillis() - lastCheckAtMillis < CHECK_INTERVAL_MILLIS) return@withContext
        lastCheckAtMillis = System.currentTimeMillis()

        runCatching { api.getLatestRelease() }
            .onSuccess { release ->
                val remoteVersionCode = release.tag_name.removePrefix("v").toIntOrNull()
                val asset = release.assets.firstOrNull { it.name.endsWith(".apk") }
                when {
                    remoteVersionCode != null && asset != null && remoteVersionCode > BuildConfig.VERSION_CODE -> {
                        _toastMessages.tryEmit("Spotlite update found (${release.tag_name}) — downloading…")
                        downloadAndInstall(asset.browser_download_url)
                    }
                    manual -> _toastMessages.tryEmit("Spotlite is up to date.")
                }
            }
            .onFailure { if (manual) _toastMessages.tryEmit("Update check failed: ${it.message}") }
    }

    private suspend fun downloadAndInstall(url: String) = withContext(Dispatchers.IO) {
        if (!context.packageManager.canRequestPackageInstalls()) {
            _toastMessages.tryEmit("Grant \"install unknown apps\" for Spotlite, then check for updates again.")
            requestInstallPermission()
            return@withContext
        }

        runCatching {
            val file = File(context.cacheDir, "spotlite-update.apk")
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body ?: throw IOException("Empty response body")
                file.outputStream().use { out -> body.byteStream().copyTo(out) }
            }
            file
        }.onSuccess { file ->
            _toastMessages.tryEmit("Update downloaded — opening installer…")
            launchInstaller(file)
        }.onFailure {
            _toastMessages.tryEmit("Update download failed: ${it.message}")
        }
    }

    private fun launchInstaller(file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    private fun requestInstallPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${context.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}

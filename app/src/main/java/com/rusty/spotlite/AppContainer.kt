package com.rusty.spotlite

import android.content.Context
import com.rusty.spotlite.auth.AuthRepository
import com.rusty.spotlite.data.TokenStore
import com.rusty.spotlite.network.NetworkModule
import com.rusty.spotlite.remote.PlaybackController
import com.rusty.spotlite.repo.LibraryRepository

/** Hand-rolled composition root; the object graph here is small enough that a DI framework isn't worth the APK weight. */
class AppContainer(context: Context) {
    val tokenStore = TokenStore(context.applicationContext)
    val authRepository = AuthRepository(context.applicationContext, tokenStore, NetworkModule.bareHttpClient)
    val libraryRepository = LibraryRepository(NetworkModule.buildSpotifyApi(authRepository))
    val playbackController = PlaybackController(context.applicationContext)
}

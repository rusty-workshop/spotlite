package com.rusty.spotlite.ui.nav

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.rusty.spotlite.AppContainer
import com.rusty.spotlite.ui.ArtistDetailViewModelFactory
import com.rusty.spotlite.ui.LibraryViewModelFactory
import com.rusty.spotlite.ui.PlaylistDetailViewModelFactory
import com.rusty.spotlite.ui.SearchViewModelFactory
import com.rusty.spotlite.ui.artist.ArtistDetailScreen
import com.rusty.spotlite.ui.artist.ArtistDetailViewModel
import com.rusty.spotlite.ui.library.LibraryScreen
import com.rusty.spotlite.ui.library.LibraryViewModel
import com.rusty.spotlite.ui.login.LoginScreen
import com.rusty.spotlite.ui.nowplaying.NowPlayingBar
import com.rusty.spotlite.ui.playlist.PlaylistDetailScreen
import com.rusty.spotlite.ui.playlist.PlaylistDetailViewModel
import com.rusty.spotlite.ui.search.SearchScreen
import com.rusty.spotlite.ui.search.SearchViewModel

@Composable
fun SpotliteNavHost(
    container: AppContainer,
    startDestination: String,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    Column(modifier = modifier.fillMaxSize()) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.weight(1f),
        ) {
            composable(Routes.LOGIN) {
                LoginScreen(authRepository = container.authRepository)
            }

            composable(Routes.LIBRARY) {
                val viewModel: LibraryViewModel = viewModel(factory = LibraryViewModelFactory(container))
                LibraryScreen(
                    viewModel = viewModel,
                    onOpenPlaylist = { navController.navigate(Routes.playlist(it.id, it.name, it.uri)) },
                    onOpenArtist = { navController.navigate(Routes.artist(it.id, it.name)) },
                    onPlayTrackUri = { container.playbackController.play(it) },
                    onOpenSearch = { navController.navigate(Routes.SEARCH) },
                )
            }

            composable(Routes.SEARCH) {
                val viewModel: SearchViewModel = viewModel(factory = SearchViewModelFactory(container))
                SearchScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onOpenPlaylist = { navController.navigate(Routes.playlist(it.id, it.name, it.uri)) },
                    onOpenArtist = { navController.navigate(Routes.artist(it.id, it.name)) },
                    onPlayTrackUri = { container.playbackController.play(it) },
                )
            }

            composable(Routes.PLAYLIST_PATTERN) { backStackEntry ->
                val playlistId = backStackEntry.arguments?.getString("playlistId").orEmpty()
                val playlistName = backStackEntry.arguments?.getString("playlistName").orEmpty()
                val playlistUri = backStackEntry.arguments?.getString("playlistUri").orEmpty()
                val viewModel: PlaylistDetailViewModel = viewModel(
                    factory = PlaylistDetailViewModelFactory(container, playlistId, playlistUri),
                )
                PlaylistDetailScreen(
                    playlistName = playlistName,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }

            composable(Routes.ARTIST_PATTERN) { backStackEntry ->
                val artistId = backStackEntry.arguments?.getString("artistId").orEmpty()
                val artistName = backStackEntry.arguments?.getString("artistName").orEmpty()
                val viewModel: ArtistDetailViewModel = viewModel(
                    factory = ArtistDetailViewModelFactory(container, artistId),
                )
                ArtistDetailScreen(
                    artistName = artistName,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                )
            }
        }

        container.playbackController.nowPlaying?.let { nowPlaying ->
            NowPlayingBar(
                nowPlaying = nowPlaying,
                onTogglePlayPause = { container.playbackController.togglePlayPause() },
                onSkipNext = { container.playbackController.skipNext() },
            )
        }
    }
}

package com.rusty.spotlite.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rusty.spotlite.AppContainer
import com.rusty.spotlite.ui.artist.ArtistDetailViewModel
import com.rusty.spotlite.ui.library.LibraryViewModel
import com.rusty.spotlite.ui.playlist.PlaylistDetailViewModel
import com.rusty.spotlite.ui.queue.QueueViewModel
import com.rusty.spotlite.ui.search.SearchViewModel
import com.rusty.spotlite.ui.settings.IconSettingsViewModel

class LibraryViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        LibraryViewModel(container.libraryRepository, container.savedTracksStore) as T
}

class PlaylistDetailViewModelFactory(
    private val container: AppContainer,
    private val playlistId: String,
    private val playlistUri: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        PlaylistDetailViewModel(
            container.libraryRepository,
            container.playbackController,
            container.savedTracksStore,
            playlistId,
            playlistUri,
        ) as T
}

class ArtistDetailViewModelFactory(
    private val container: AppContainer,
    private val artistId: String,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ArtistDetailViewModel(
            container.libraryRepository,
            container.playbackController,
            artistId,
        ) as T
}

class SearchViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        SearchViewModel(container.libraryRepository, container.savedTracksStore) as T
}

class QueueViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        QueueViewModel(container.libraryRepository) as T
}

class IconSettingsViewModelFactory(private val container: AppContainer) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        IconSettingsViewModel(container.appIconManager, container.updateManager) as T
}

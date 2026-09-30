package com.rusty.spotlite.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rusty.spotlite.BuildConfig
import com.rusty.spotlite.icon.AppIconManager
import com.rusty.spotlite.icon.IconVariant
import com.rusty.spotlite.update.UpdateManager
import kotlinx.coroutines.launch

class IconSettingsViewModel(
    private val appIconManager: AppIconManager,
    private val updateManager: UpdateManager,
) : ViewModel() {

    var selected by mutableStateOf(appIconManager.currentVariant())
        private set

    val versionName: String = BuildConfig.VERSION_NAME

    fun select(variant: IconVariant) {
        if (variant == selected) return
        appIconManager.setVariant(variant)
        selected = variant
    }

    fun checkForUpdate() {
        viewModelScope.launch { updateManager.checkForUpdate(manual = true) }
    }
}

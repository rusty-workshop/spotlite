package com.rusty.spotlite.ui.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.rusty.spotlite.icon.AppIconManager
import com.rusty.spotlite.icon.IconVariant

class IconSettingsViewModel(private val appIconManager: AppIconManager) : ViewModel() {

    var selected by mutableStateOf(appIconManager.currentVariant())
        private set

    fun select(variant: IconVariant) {
        if (variant == selected) return
        appIconManager.setVariant(variant)
        selected = variant
    }
}

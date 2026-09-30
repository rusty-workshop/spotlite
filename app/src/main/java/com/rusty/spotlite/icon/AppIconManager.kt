package com.rusty.spotlite.icon

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager

/**
 * Switches the home screen launcher icon via activity-alias enable/disable — Android has
 * no other public API for changing an installed app's icon at runtime. Exactly one alias
 * (see IconVariant) is enabled at any time; the rest are disabled in the manifest by default.
 */
class AppIconManager(private val context: Context) {

    fun currentVariant(): IconVariant =
        IconVariant.entries.firstOrNull { isEnabled(it) } ?: IconVariant.CLASSIC

    fun setVariant(variant: IconVariant) {
        // Enable the new one first, then disable the rest — avoids a window where the
        // app has zero enabled launcher components if this gets interrupted midway.
        setEnabled(variant, true)
        IconVariant.entries.filter { it != variant }.forEach { setEnabled(it, false) }
    }

    private fun isEnabled(variant: IconVariant): Boolean {
        val state = context.packageManager.getComponentEnabledSetting(componentName(variant))
        return when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED -> false
            // COMPONENT_ENABLED_STATE_DEFAULT: fall back to what the manifest declares.
            else -> variant == IconVariant.CLASSIC
        }
    }

    private fun setEnabled(variant: IconVariant, enabled: Boolean) {
        context.packageManager.setComponentEnabledSetting(
            componentName(variant),
            if (enabled) PackageManager.COMPONENT_ENABLED_STATE_ENABLED else PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            // DONT_KILL_APP: switching icons shouldn't kill the foreground session the
            // user is mid-action in, even though some launchers still need a trip home
            // to actually repaint the icon.
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun componentName(variant: IconVariant) = ComponentName(context.packageName, variant.aliasName)
}

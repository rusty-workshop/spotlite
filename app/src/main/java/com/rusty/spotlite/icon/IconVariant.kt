package com.rusty.spotlite.icon

import com.rusty.spotlite.R

/**
 * One launcher icon color scheme. [aliasName] must exactly match an
 * `<activity-alias android:name="...">` in AndroidManifest.xml — AppIconManager
 * enables exactly one of these and disables the rest to switch the home screen icon.
 */
enum class IconVariant(
    val aliasName: String,
    val label: String,
    val previewIconRes: Int,
) {
    CLASSIC(
        aliasName = "com.rusty.spotlite.icon.IconClassic",
        label = "Classic",
        previewIconRes = R.mipmap.ic_launcher,
    ),
    MIDNIGHT(
        aliasName = "com.rusty.spotlite.icon.IconMidnight",
        label = "Midnight",
        previewIconRes = R.mipmap.ic_launcher_midnight,
    ),
    SUNSET(
        aliasName = "com.rusty.spotlite.icon.IconSunset",
        label = "Sunset",
        previewIconRes = R.mipmap.ic_launcher_sunset,
    ),
    OCEAN(
        aliasName = "com.rusty.spotlite.icon.IconOcean",
        label = "Ocean",
        previewIconRes = R.mipmap.ic_launcher_ocean,
    ),
    NOIR(
        aliasName = "com.rusty.spotlite.icon.IconNoir",
        label = "Noir",
        previewIconRes = R.mipmap.ic_launcher_noir,
    ),
}

# Spotify App Remote SDK

This AAR isn't on Maven Central or JitPack — Spotify only ships it as a raw
download. Grab it once and drop it here:

1. https://github.com/spotify/android-sdk/releases — download the latest
   release zip.
2. Unzip it, then copy `spotify-app-remote-release-X.X.X.aar` into this
   `app/libs/` folder (keep the filename, Gradle's fileTree picks up any
   `*.aar` here automatically).
3. That's it — no Gradle changes needed, `app/build.gradle.kts` already
   includes everything in this directory.

You'll also need the Spotify app itself installed (and logged into Premium)
on any device this runs on, since App Remote controls the real Spotify app
rather than streaming audio itself.

# Spotlite

A lightweight, native Android Spotify client. Browses your entire library —
every playlist, every saved song, every followed artist — through the Spotify
Web API, and plays tracks by driving the real Spotify app via the App Remote
SDK, instead of trying to stream audio itself.

Built to feel fast on low-end phones: no Paging3, no Glide, no dynamic
Material You theming, no large images decoded for small rows. Release build
is ~1.5MB.

## Why it needs the Spotify app installed

Spotify's public API has no way for a third-party app to stream audio itself
— only the **App Remote SDK** can do that, and it works by remote-controlling
a real, already-installed Spotify app (Premium required) rather than playing
audio in-process. So Spotlite is a custom, fast browsing UI in front of the
Spotify app's actual playback engine, not a replacement for it.

## One-time setup

1. **Create a Spotify app** at https://developer.spotify.com/dashboard →
   Create app.
   - Add a Redirect URI: `spotlite://callback`
   - Enable the **Android** platform, package name `com.rusty.spotlite`, and
     give it your device's SHA-1 debug signing fingerprint (get it with
     `./gradlew signingReport`).
   - Copy the **Client ID**.
2. Paste that Client ID into
   [`app/src/main/java/com/rusty/spotlite/Config.kt`](app/src/main/java/com/rusty/spotlite/Config.kt).
3. **Download the App Remote SDK AAR** — see
   [`app/libs/README.md`](app/libs/README.md) for the one-time download step
   (it's not on Maven Central, so Gradle can't fetch it for you).
4. Build and install:
   ```
   ./gradlew installDebug
   ```
5. Make sure the real Spotify app is installed and logged into Premium on the
   same device, then log in inside Spotlite.

## Architecture

- `auth/` — hand-rolled OAuth PKCE login (no client secret needed, no third
  party auth library)
- `network/` + `model/` — Retrofit + kotlinx.serialization client for the
  Web API (playlists, liked songs, followed artists, artist top tracks)
- `remote/` — wraps the App Remote SDK for play/pause/skip and now-playing
  state
- `repo/` — flattens Spotify's offset/cursor pagination into simple pages
- `ui/` — Compose screens (Login, Library tabs, Playlist detail, Artist
  detail) plus a hand-rolled `OffsetPager` that drives infinite-scroll lists
  without pulling in the Paging3 dependency chain

## License

AGPL-3.0-or-later — see [LICENSE](LICENSE).

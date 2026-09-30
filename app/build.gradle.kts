import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
}

// Read from local.properties (gitignored) rather than hardcoding a personal Spotify app's
// Client ID in a tracked source file — it's not secret (PKCE needs no client secret), but
// it's still tied to one person's developer dashboard and shouldn't end up in the repo.
val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) load(FileInputStream(file))
}
val spotifyClientId: String = localProperties.getProperty("spotify.clientId", "")

// versionCode tracks git history directly — every commit on main gets its own strictly
// increasing code for free, so the in-app updater always knows a CI-built release is
// newer than whatever's installed without anyone hand-bumping a number.
val gitCommitCount: Int = try {
    val process = ProcessBuilder("git", "rev-list", "--count", "HEAD")
        .directory(rootDir)
        .redirectErrorStream(true)
        .start()
    process.inputStream.bufferedReader().readText().trim().toInt().also { process.waitFor() }
} catch (e: Exception) {
    1
}

val releaseStoreFile = localProperties.getProperty("release.storeFile", "")
val releaseStorePassword = localProperties.getProperty("release.storePassword", "")
val releaseKeyAlias = localProperties.getProperty("release.keyAlias", "")
val releaseKeyPassword = localProperties.getProperty("release.keyPassword", "")
val hasReleaseSigningConfig = releaseStoreFile.isNotBlank() && releaseStorePassword.isNotBlank()

android {
    namespace = "com.rusty.spotlite"

    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    defaultConfig {
        applicationId = "com.rusty.spotlite"
        minSdk = 26
        targetSdk = 34
        versionCode = gitCommitCount
        versionName = "1.$gitCommitCount"

        buildConfigField("String", "SPOTIFY_CLIENT_ID", "\"$spotifyClientId\"")
    }

    // Only defined when local.properties (or CI) actually supplies keystore credentials,
    // so a fresh clone without them still builds an (unsigned) release APK — same
    // fallback pattern as the Spotify Client ID and the App Remote AAR.
    if (hasReleaseSigningConfig) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(releaseStoreFile)
                storePassword = releaseStorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            if (hasReleaseSigningConfig) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }
}

// The Spotify App Remote SDK is distributed as a raw AAR from Spotify's GitHub
// releases, not through Maven Central or JitPack — see app/libs/README.md.
dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))

    implementation(platform(libs.androidx.compose.bom))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.browser)

    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    implementation(libs.okhttp)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)

    // Coil: chosen over Glide for lower memory overhead and native Compose
    // support — matters on the low-end phones this app targets.
    implementation(libs.coil.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
}

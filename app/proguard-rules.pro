# kotlinx.serialization keeps its generated serializers reflectively.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class com.rusty.spotlite.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Spotify App Remote SDK talks to the Spotify app over AIDL/protobuf internals.
-keep class com.spotify.protocol.** { *; }
-keep class com.spotify.android.appremote.** { *; }

# The AAR's ConnectionParams constructor actually requires Gson at runtime (see the
# explicit dependency in app/build.gradle.kts — it's not optional despite looking like
# one of several interchangeable mapper backends). Jackson is a genuine alternative
# backend it never ends up using here, so it stays un-added; -dontwarn covers both since
# R8 can't otherwise tell which of the two is real from this codebase alone.
-dontwarn com.fasterxml.jackson.**
-dontwarn com.google.gson.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.spotify.base.annotations.**

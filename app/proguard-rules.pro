# kotlinx.serialization keeps its generated serializers reflectively.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclasseswithmembers class com.rusty.spotlite.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Spotify App Remote SDK talks to the Spotify app over AIDL/protobuf internals.
-keep class com.spotify.protocol.** { *; }
-keep class com.spotify.android.appremote.** { *; }

# The AAR supports optional Jackson/Gson-based JSON mappers it picks at runtime
# via reflection; neither library is on our classpath (Retrofit uses kotlinx
# serialization instead), so these references are dead code paths R8 can't see.
-dontwarn com.fasterxml.jackson.**
-dontwarn com.google.gson.**
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.spotify.base.annotations.**

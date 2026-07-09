# EventSnap R8/ProGuard rules for release (minify + resource shrink).
# The app extracts events by reflecting over model classes at runtime (Moshi reflective adapter,
# kotlinx-serialization nav routes, Retrofit), so those must be kept or extraction breaks silently.

# ---- Kotlin metadata (needed by Moshi reflection & kotlinx-serialization) ----
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep class kotlin.Metadata { *; }

# ---- Moshi (reflection adapter + codegen) ----
# Keep Moshi internals and generated JsonAdapters; don't warn on its optional deps.
-keep class com.squareup.moshi.** { *; }
-keep interface com.squareup.moshi.** { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson <methods>;
    @com.squareup.moshi.ToJson <methods>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keep class **JsonAdapter { *; }
-dontwarn com.squareup.moshi.**

# Our Groq DTOs are (de)serialized by name — keep them and their members intact.
-keep class com.eventsnap.android.core.data.groq.** { *; }

# ---- kotlinx-serialization (Navigation 3 @Serializable routes) ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class **$$serializer { *; }
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.eventsnap.android.**$$serializer { *; }
-keepclassmembers class com.eventsnap.android.** {
    *** Companion;
}

# ---- Retrofit / OkHttp ----
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

# ---- Koin ----
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# ---- Tink / EncryptedSharedPreferences (compile-only errorprone annotations) ----
-dontwarn com.google.errorprone.annotations.**
-dontwarn com.google.crypto.tink.**

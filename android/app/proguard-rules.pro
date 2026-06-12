# Keep readable stack traces in release crash reports.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── kotlinx-serialization ─────────────────────────────────────────────────────
# The plugin generates serializers looked up reflectively via Companion fields.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers @kotlinx.serialization.Serializable class com.whatshappening.novisad.** {
    *** Companion;
    *** INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}
-if @kotlinx.serialization.Serializable class com.whatshappening.novisad.**
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}

# ── MapLibre ──────────────────────────────────────────────────────────────────
# Native code calls back into these classes via JNI; names must survive.
-keep class org.maplibre.android.** { *; }
-keep class org.maplibre.geojson.** { *; }
-dontwarn org.maplibre.**

# MapLibre's GeoJSON layer serializes via Gson reflection.
-keep class com.google.gson.** { *; }
-keepattributes Signature
-dontwarn com.google.gson.**

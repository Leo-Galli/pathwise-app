# ─────────────────────────────────────────────────────────────
#  PathWise — ProGuard / R8 rules
# ─────────────────────────────────────────────────────────────

# Google Maps SDK: mantiene le classi usate tramite reflection dai manifest
-keep class com.google.android.gms.maps.** { *; }
-keep class com.google.android.libraries.mapsplatform.** { *; }
-keep class com.google.maps.android.compose.** { *; }

# Health Connect: modello dati usato via reflection/parcelable
-keep class androidx.health.connect.client.** { *; }

# Room: gli entity vengono istanziati via reflection nei TypeConverters
-keep class it.leogalli.pathwise.data.local.entity.** { *; }

# Kotlinx Coroutines
-dontwarn org.jetbrains.annotations.**

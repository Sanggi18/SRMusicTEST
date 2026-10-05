# ===================================================================
# FULL OBFUSCATION & REPACKAGING CONFIGURATION (ALAS RETRO MUSIC)
# ===================================================================
# Custom Barcode Obfuscation Dictionary (I and l)
-obfuscationdictionary obfuscation-dictionary.txt
-classobfuscationdictionary obfuscation-dictionary.txt
-packageobfuscationdictionary obfuscation-dictionary.txt

# Repackage semua class yang diobfuscate ke direktori root
-repackageclasses
-allowaccessmodification
-overloadaggressively
-renamesourcefileattribute ""

# Preserve stack traces line numbers
-keepattributes SourceFile,LineNumberTable

# Entry points AndroidManifest yang dipanggil langsung oleh OS:
-keep public class com.example.MainActivity
-keep public class com.example.SRMusicApp
-keep public class com.example.core.playback.MusicService

# Media3 Session Service (A)
# Mengandalkan consumer rules bawaan Media3 AAR. Cukup keep Session Service & Session.
-keep class androidx.media3.session.MediaSessionService { *; }
-keep class androidx.media3.session.MediaSession { *; }
-dontwarn androidx.media3.**

# Jellyfin Media3 Native Decoder (B)
# Persempit keep hanya pada package FFmpeg renderer dan JNI bindings yang dipakai.
-keep class org.jellyfin.media3.ffmpeg.** { *; }
-dontwarn org.jellyfin.media3.**

# Keep JNI Methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Glide
-keep public class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
    **[] $VALUES;
    public *;
}
-dontwarn com.bumptech.glide.**

# Jaudiotagger (C - Dipertahankan aman untuk refleksi tag dan ID3 frame bodies)
-dontwarn org.jaudiotagger.**
-keep class org.jaudiotagger.** { *; }

# Keep Compose view essentials
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView { *; }

# Room Database & Entities (D)
# Room men-generate implementasi langsung saat compile-time via KSP.
-keep class com.example.core.data.database.AppDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-keep class * extends androidx.room.RoomDatabase
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <methods>;
}
-dontwarn androidx.room.paging.**

# Keep enums and entries for Kotlin 1.9+ and Android (E)
# Preserve enum reflection methods
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    public static ** getEntries();
    public static **[] $VALUES;
}

# Pertahankan enum yang dipersistensikan ke SharedPreferences atau Settings
-keep enum com.example.core.model.NowPlayingProgressStyle { *; }
-keep enum com.example.core.model.NowPlayingTheme { *; }
-keep enum com.example.core.model.TrackInfoAlignment { *; }
-keep enum com.example.core.model.AppThemeMode { *; }
-keep enum com.example.core.model.AppSortCriteria { *; }
-keep enum com.example.core.model.AppFolderStyle { *; }
-keep enum com.example.core.model.AppGridMode { *; }
-keep enum com.example.ui.common.theme.ThemeMode { *; }
-keep enum com.example.ui.navigation.components.TabItem { *; }

# Parcelable CREATORs (F)
-keepclassmembers class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

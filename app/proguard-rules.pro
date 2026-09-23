# Room SQLite rules
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# Moshi rules
-dontwarn java.lang.invoke.*
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <methods>;
}
-keepclasseswithmembers class * {
    @com.squareup.moshi.* <fields>;
}
-keep @com.squareup.moshi.JsonClass class * { *; }
-keepclassmembers class * {
    @com.squareup.moshi.FromJson *;
    @com.squareup.moshi.ToJson *;
}

# Keep Data Models and Entity Classes
-keep class com.example.data.model.** { *; }
-keep class com.example.data.ai.** { *; }
-keep class com.example.data.importer.** { *; }
-keep class com.example.data.remote.** { *; }

# OkHttp rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Coroutines rules
-dontwarn kotlinx.coroutines.**
